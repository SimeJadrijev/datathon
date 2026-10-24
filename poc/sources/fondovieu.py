"""fondovieu.gov.hr: pozivi iz Nacionalnog plana oporavka i otpornosti (NPOO).

Nuxt aplikacija sa server-side renderiranjem, pa za popis ne treba JS:
`/pozivi?page=N&statusGroup=otvoreni|uNajavi|zatvoreni` (10 po stranici).
robots.txt zabranjuje `/download`, pa se prilozi ne preuzimaju.
"""
import re

from selectolax.lexbor import LexborHTMLParser

import config
from common.http import get_client
from common.models import RawItem
from common.text import clean, first_date
from sources.base import fill_from_page

IZVOR = "fondovieu"
DAVATELJ = "Ministarstvo regionalnoga razvoja i fondova Europske unije (NPOO)"
METODA = "server-renderirani HTML (Nuxt SSR), paginacija"
BASE = "https://fondovieu.gov.hr"
# "svi" zadnji: stavke koje nisu u otvorenima ni zatvorenima ostaju bez statusa (pravila ga izvode iz roka).
# Vrijednost grupe za najavljene pozive nije pronađena (nepoznat parametar vraća sve pozive).
GRUPE = {"otvoreni": "otvoren", "zatvoreni": "zatvoren", "svi": None}


def _parse(html: str, status: str) -> list[RawItem]:
    out = []
    tree = LexborHTMLParser(html)
    for tr in tree.css("table tr"):
        a = tr.css_first('a[href^="/pozivi/"]')
        if a is None:
            continue
        pid = a.attributes["href"].rsplit("/", 1)[-1]
        if not pid.isdigit():
            continue
        ps = [clean(p.text()) for p in tr.css("p")]
        naziv = next((clean(p.text()) for p in tr.css("p.font-semibold")), "")
        kod = next((p.split(":", 1)[1].strip() for p in ps if p.startswith("Kod poziva")), None)
        tip = next((p.split(":", 1)[1].strip() for p in ps if p.startswith("Tip natječaja")), None)
        rok = next((p for p in ps if p.startswith("Rok za prijave")), "")
        out.append(RawItem(
            izvor=IZVOR, id=f"{IZVOR}-{pid}", url=f"{BASE}/pozivi/{pid}", naziv=naziv,
            davatelj=DAVATELJ, status_hint=status, rok_hint=first_date(rok),
            vrsta_hint="javni poziv", zupanije_hint=["sve"],
            extra={"kod_poziva": kod, "tip": tip},
        ))
    return out


def fetch_list() -> list[RawItem]:
    http = get_client()
    items: dict[str, RawItem] = {}
    for grupa, status in GRUPE.items():
        for page in range(1, config.MAX_LIST_PAGES + 1):
            html = http.get_text(f"{BASE}/pozivi?page={page}&statusGroup={grupa}")
            batch = [i for i in _parse(html, status) if i.id not in items]
            if not batch:
                break
            for i in batch:
                items[i.id] = i
    return list(items.values())


def fetch_detail(item: RawItem) -> RawItem:
    fill_from_page(item, ("main", "#__nuxt"))
    # prilozi idu kroz /download (zabranjeno u robots.txt) -> zadrži samo vanjske poveznice
    item.prilozi = [p for p in item.prilozi if "/download" not in p["url"]]
    m = re.search(r"Datum objave[:\s]*([\d.\s]+)", item.tekst)
    if m:
        item.datum_objave = first_date(m.group(1))
    m = re.search(r"Nadležno tijelo:\s*\n(.+)", item.tekst)
    if m:
        item.davatelj = m.group(1).strip()
    return item
