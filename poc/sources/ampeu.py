"""AMPEU (Agencija za mobilnost i programe EU): Erasmus+, CEEPUS, bilateralne stipendije.

Craft CMS, statični HTML. Popis /natjecaji (+ /natjecaji/pN) u blokovima
"otvoreni"/"zatvoreni"; kartica ima program, datum (<time datetime>), naslov i
"Rok je prošao" za zatvorene.
"""
from selectolax.lexbor import LexborHTMLParser

import config
from common.http import get_client
from common.models import RawItem
from common.text import clean
from sources.base import fill_from_page, looks_like_funding

IZVOR = "ampeu"
DAVATELJ = "Agencija za mobilnost i programe Europske unije"
METODA = "statični HTML (Craft CMS), paginacija"
BASE = "https://www.ampeu.hr/natjecaji"


def fetch_list() -> list[RawItem]:
    http = get_client()
    items: dict[str, RawItem] = {}
    preskoceno = 0
    for page in range(1, config.MAX_LIST_PAGES + 1):
        url = BASE if page == 1 else f"{BASE}/p{page}"
        try:
            tree = LexborHTMLParser(http.get_text(url))
        except Exception:
            break
        new = 0
        for card in tree.css("div.NewsItem"):
            a = card.css_first("a.NewsItem__link")
            if a is None or "/natjecaji/" not in a.attributes.get("href", ""):
                continue
            href = a.attributes["href"]
            iid = f"{IZVOR}-{href.rstrip('/').rsplit('/', 1)[-1][:80]}"
            if iid in items:
                continue
            new += 1
            naziv = clean((card.css_first(".NewsItem__title") or a).text())
            if not looks_like_funding(naziv) and "stipend" not in naziv.lower():
                preskoceno += 1
                continue
            t = card.css_first("time")
            info = clean((card.css_first(".NewsItem__info") or card).text()) if card.css_first(".NewsItem__info") else ""
            program = clean(card.css_first(".NewsItem__meta strong").text()) if card.css_first(".NewsItem__meta strong") else None
            items[iid] = RawItem(
                izvor=IZVOR, id=iid, url=href, naziv=naziv, davatelj=DAVATELJ,
                datum_objave=t.attributes.get("datetime") if t else None,
                status_hint="zatvoren" if "prošao" in info.lower() else None,
                vrsta_hint="stipendija" if "stipend" in naziv.lower() else None,
                kategorije=[program] if program else [], zupanije_hint=["sve"],
            )
        if new == 0:
            break
    out = list(items.values())
    if out:
        out[0].extra["preskoceno_nije_financiranje"] = preskoceno
    return out


def fetch_detail(item: RawItem) -> RawItem:
    return fill_from_page(item, ("main .Article", "main article", "main"))
