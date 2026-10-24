"""Grad Zagreb.

Nema jedinstvenog popisa: natječaji su razbacani po tematskim stranicama
(poduzetnici, stipendije, potpore obrazovanju, udruge...), svaka ručno uređena.
POC uzima nekoliko tematskih stranica i s njih linkove oblika /naslov/ID.
Stranice nemaju robots.txt (404), CMS je vlastiti, HTML bez semantičkih klasa.
"""
import re

from selectolax.lexbor import LexborHTMLParser

from common.http import get_client
from common.models import RawItem
from common.text import clean
from sources.base import fill_from_page, looks_like_funding

IZVOR = "zagreb"
DAVATELJ = "Grad Zagreb"
METODA = "statični HTML, više ručno uređenih tematskih stranica"
STRANICE = [
    "https://www.zagreb.hr/javni-pozivi-za-poduzetnike/200805",
    "https://www.zagreb.hr/stipendije-grada-zagreba/175198",
    "https://www.zagreb.hr/javni-pozivi-za-dodjelu-potpore-obrazovanju/188776",
    "https://www.zagreb.hr/javni-pozivi/136001",
]
LINK_RE = re.compile(r"^(?:https?://(?:www\.)?zagreb\.hr)?/[a-z0-9%-]{10,}/(\d{5,})$")


def fetch_list() -> list[RawItem]:
    http = get_client()
    items: dict[str, RawItem] = {}
    for url in STRANICE:
        tree = LexborHTMLParser(http.get_text(url))
        # popis je u div.newswrap (ostatak stranice je navigacija s istim oblikom linkova)
        scopes = tree.css("div.newswrap") or tree.css("div.sadrzaj")
        for a in (a for sc in scopes for a in sc.css("a[href]")):
            href = a.attributes["href"].strip()
            m = LINK_RE.match(href)
            naziv = clean(a.text())
            if not m or not naziv or not looks_like_funding(naziv):
                continue
            iid = f"{IZVOR}-{m.group(1)}"
            items.setdefault(iid, RawItem(
                izvor=IZVOR, id=iid, url="https://www.zagreb.hr" + href if href.startswith("/") else href,
                naziv=naziv, davatelj=DAVATELJ, zupanije_hint=["Grad Zagreb"],
                vrsta_hint="stipendija" if "stipendij" in naziv.lower() else None,
                extra={"stranica": url},
            ))
    return list(items.values())


def fetch_detail(item: RawItem) -> RawItem:
    return fill_from_page(item, ("div.clanak_container", "div.clanak", "div.sadrzaj"))
