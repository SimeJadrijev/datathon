"""APPRRR (Agencija za plaćanja u poljoprivredi, ribarstvu i ruralnom razvoju).

RSS (https://www.apprrr.hr/feed/) ima samo zadnjih 10 objava, pa se za arhivu
koristi WordPress REST API istog sjedišta (javni, bez ključa):
kategorija `natjecaji` (id 155) cijela + `obavijesti` (167) filtrirane po naslovu.
"""
import re

import feedparser
import httpx
from selectolax.lexbor import LexborHTMLParser

import config
from common.http import get_client
from common.models import RawItem
from common.text import clean, html_to_text
from sources.base import attachments, looks_like_funding

IZVOR = "apprrr"
DAVATELJ = "Agencija za plaćanja u poljoprivredi, ribarstvu i ruralnom razvoju"
METODA = "RSS + WordPress REST API"
API = "https://www.apprrr.hr/wp-json/wp/v2/posts"
KATEGORIJE = {155: "natjecaji", 167: "obavijesti"}


def _posts(cat: int):
    http = get_client()
    for page in range(1, config.MAX_LIST_PAGES + 1):
        try:
            batch = http.get_json(f"{API}?categories={cat}&per_page=100&page={page}&_fields=id,date,link,title,content,categories")
        except httpx.HTTPStatusError:
            break          # WP vraća 400 kad stranica ne postoji
        if not batch:
            break
        yield from batch
        if len(batch) < 100:
            break


def _item(p: dict, kat: str) -> RawItem:
    html = p["content"]["rendered"]
    node = LexborHTMLParser(html).body
    return RawItem(
        izvor=IZVOR, id=f"{IZVOR}-{p['id']}", url=p["link"], naziv=clean(p["title"]["rendered"]),
        davatelj=DAVATELJ, datum_objave=p["date"][:10], kategorije=[kat],
        zupanije_hint=["sve"], tekst=html_to_text(node), prilozi=attachments(node, p["link"]),
        detalji_dohvaceni=True,
        extra={"poveznice": re.findall(r'href="(https://www\.apprrr\.hr/(?:intervencije|mjere)/[^"]+)"', html)},
    )


def fetch_list() -> list[RawItem]:
    items: dict[str, RawItem] = {}
    for p in _posts(155):
        it = _item(p, "natjecaji")
        items[it.id] = it
    for p in _posts(167):
        title = clean(p["title"]["rendered"])
        if looks_like_funding(title):
            it = _item(p, "obavijesti")
            items.setdefault(it.id, it)
    # RSS: provjera da feed radi i da su svježe objave pokrivene
    feed = feedparser.parse(get_client().get_bytes("https://www.apprrr.hr/feed/", ".xml", config.HTML_CACHE_TTL_H))
    rss_ids = {e.get("id") or e.link for e in feed.entries}
    for it in items.values():
        it.extra["u_rss"] = it.url in {e.link for e in feed.entries}
    items_list = list(items.values())
    if items_list:
        items_list[0].extra["rss_stavki"] = len(rss_ids)
    return items_list


def fetch_detail(item: RawItem) -> RawItem:
    """Objava ima puni tekst, ali dokumentacija je na stranici intervencije/mjere."""
    if item.prilozi or not item.extra.get("poveznice"):
        return item
    url = item.extra["poveznice"][0]
    node = LexborHTMLParser(get_client().get_text(url)).css_first("main, article, .entry-content, body")
    item.prilozi = attachments(node, url)
    item.extra["stranica_dokumenata"] = url
    return item
