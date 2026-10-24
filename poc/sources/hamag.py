"""HAMAG-BICRO: potpore, zajmovi i jamstva poduzetnicima.

WordPress bez javnog REST API-ja (404). Popis /natjecaji/page/N/ (masonry grid),
kategorija i status u CSS klasama (`category-otvoreni-natjecaji`).
Dio stavki vodi na eufondovi.gov.hr (novi portal EU fondova 2021.-2027.).
robots.txt zabranjuje /arhiva/.
"""
from selectolax.lexbor import LexborHTMLParser

import config
from common.http import get_client
from common.models import RawItem
from common.text import clean, first_date
from sources.base import fill_from_page

IZVOR = "hamag"
DAVATELJ = "HAMAG-BICRO"
METODA = "statični HTML (WordPress), paginacija"
BASE = "https://hamagbicro.hr/natjecaji/"


def fetch_list() -> list[RawItem]:
    http = get_client()
    items: dict[str, RawItem] = {}
    for page in range(1, config.MAX_LIST_PAGES + 1):
        url = BASE if page == 1 else f"{BASE}page/{page}/"
        try:
            html = http.get_text(url)
        except Exception:
            break
        tree = LexborHTMLParser(html)
        new = 0
        for w in tree.css("div.post-wrapper"):
            a = w.css_first("h3 a")
            if a is None:
                continue
            classes = (w.attributes.get("class") or "").split()
            pid = (w.attributes.get("id") or "").replace("post-", "")
            iid = f"{IZVOR}-{pid}"
            if iid in items:
                continue
            kat = [c[len("category-"):] for c in classes if c.startswith("category-")]
            date_node = w.css_first(".ff-meta-item[class*=ffb-date]")
            status = "otvoren" if "otvoreni-natjecaji" in kat else ("zatvoren" if "zatvoreni-natjecaji" in kat else None)
            naziv = clean(a.text())
            if status is None and naziv.lower().startswith("najava"):
                status = "najavljen"
            items[iid] = RawItem(
                izvor=IZVOR, id=iid, url=a.attributes["href"].split("#")[0], naziv=naziv,
                davatelj=DAVATELJ, datum_objave=first_date(date_node.text()) if date_node else None,
                status_hint=status, kategorije=kat, zupanije_hint=["sve"],
            )
            new += 1
        if new == 0:
            break
    return list(items.values())


def fetch_detail(item: RawItem) -> RawItem:
    if "hamagbicro.hr" not in item.url:
        item.extra["vanjski_link"] = item.url     # eufondovi.gov.hr, nije u opsegu POC-a
        return item
    return fill_from_page(item, (".post-content", "article", ".entry-content", "main"))
