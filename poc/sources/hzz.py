"""HZZ mjere aktivne politike zapošljavanja (mjere.hr -> mjere.hzz.hr).

WordPress s javnim REST API-jem i vlastitim tipom `measures`; taksonomije
`measures_years` (godina) i `measures_category` (kome je mjera namijenjena).
Mjere iz tekuće godine smatraju se otvorenima (HZZ ih objavljuje kao kontinuirane).
"""
from datetime import date

from selectolax.lexbor import LexborHTMLParser

from common.http import get_client
from common.models import RawItem
from common.text import clean, html_to_text
from sources.base import attachments

IZVOR = "hzz"
DAVATELJ = "Hrvatski zavod za zapošljavanje"
METODA = "WordPress REST API (custom post type `measures`)"
API = "https://mjere.hzz.hr/wp-json/wp/v2"


def fetch_list() -> list[RawItem]:
    http = get_client()
    godine = {t["id"]: t["name"] for t in http.get_json(f"{API}/measures_years?per_page=100")}
    kategorije = {t["id"]: t["name"] for t in http.get_json(f"{API}/measures_category?per_page=100")}
    items = []
    for page in range(1, 6):
        batch = http.get_json(f"{API}/measures?per_page=100&page={page}")
        for m in batch:
            html = m["content"]["rendered"]
            node = LexborHTMLParser(html).body
            god = [godine.get(g, "") for g in m.get("measures_years", [])]
            tekuca = f"{date.today().year}." in god
            items.append(RawItem(
                izvor=IZVOR, id=f"{IZVOR}-{m['id']}", url=m["link"], naziv=clean(m["title"]["rendered"]),
                davatelj=DAVATELJ, datum_objave=m["date"][:10],
                status_hint="otvoren" if tekuca else "zatvoren", vrsta_hint="poticaj",
                kategorije=[kategorije.get(c, str(c)) for c in m.get("measures_category", [])],
                zupanije_hint=["sve"], tekst=html_to_text(node), prilozi=attachments(node, m["link"]),
                detalji_dohvaceni=True, extra={"godine": god},
            ))
        if len(batch) < 100:
            break
    return items


def fetch_detail(item: RawItem) -> RawItem:
    return item
