"""Grad Rijeka.

Dva kanala: (1) stranica "Ostali natječaji" (+ arhiva), tip objave `bidding`,
koji nije izložen u REST API-ju, pa HTML; (2) vijesti preko WordPress REST API-ja
(`/wp-json/wp/v2/posts?search=`), filtrirane po naslovu, jer Grad o potporama
i stipendijama često obavještava samo vijestima.
"""
import re

import httpx
from selectolax.lexbor import LexborHTMLParser

from common.http import get_client
from common.models import RawItem
from common.text import clean, html_to_text
from sources.base import attachments, fill_from_page, looks_like_funding

IZVOR = "rijeka"
DAVATELJ = "Grad Rijeka"
METODA = "statični HTML + WordPress REST API (vijesti)"
STRANICE = [
    "https://www.rijeka.hr/gradska-uprava/natjecaji-2/ostali-natjecaji/",
    "https://www.rijeka.hr/gradska-uprava/natjecaji-2/ostali-natjecaji/arhiva/",
]
API = "https://www.rijeka.hr/wp-json/wp/v2/posts"
POJMOVI = ["javni poziv", "natječaj", "potpore", "stipendije", "sufinanciranje"]


def fetch_list() -> list[RawItem]:
    http = get_client()
    items: dict[str, RawItem] = {}
    for url in STRANICE:
        tree = LexborHTMLParser(http.get_text(url))
        for a in tree.css('a[href*="/bidding/"]'):
            href = a.attributes["href"]
            naziv = clean(a.text())
            slug = href.rstrip("/").rsplit("/", 1)[-1]
            if not naziv or not looks_like_funding(naziv):
                continue
            items.setdefault(f"{IZVOR}-b-{slug[:70]}", RawItem(
                izvor=IZVOR, id=f"{IZVOR}-b-{slug[:70]}", url=href, naziv=naziv, davatelj=DAVATELJ,
                status_hint="zatvoren" if "arhiva" in url else None, zupanije_hint=["Primorsko-goranska"],
                kategorije=["natjecaji"],
            ))
    for q in POJMOVI:
        try:
            posts = http.get_json(f"{API}?search={q.replace(' ', '%20')}&per_page=100&_fields=id,date,link,title,content")
        except httpx.HTTPStatusError:
            continue
        for p in posts:
            naziv = clean(p["title"]["rendered"])
            if not looks_like_funding(naziv) or not re.search(r"poziv|natje|potpor|stipend|sufinanc|prijav", naziv, re.I):
                continue
            node = LexborHTMLParser(p["content"]["rendered"]).body
            items.setdefault(f"{IZVOR}-{p['id']}", RawItem(
                izvor=IZVOR, id=f"{IZVOR}-{p['id']}", url=p["link"], naziv=naziv, davatelj=DAVATELJ,
                datum_objave=p["date"][:10], zupanije_hint=["Primorsko-goranska"], kategorije=["vijest"],
                tekst=html_to_text(node), prilozi=attachments(node, p["link"]), detalji_dohvaceni=True,
            ))
    return list(items.values())


def fetch_detail(item: RawItem) -> RawItem:
    if item.detalji_dohvaceni:
        return item
    return fill_from_page(item, ("main article", "article", "main"))
