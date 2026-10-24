"""Ministarstvo znanosti, obrazovanja i mladih: natječaji i javni pozivi (uklj. državne stipendije).

Isti CMS kao udruge.gov.hr: HTML popis s paginacijom (`?trazi=1&page=N`, ~1.500 stavki od 2012.).
Stavka ima naziv, "Rok:", datum objave, tip i temu. Popis miješa i oglase za posao,
koji se filtriraju po naslovu i tipu.
"""
import re

from selectolax.lexbor import LexborHTMLParser

import config
from common.http import get_client
from common.models import RawItem
from common.text import clean, first_date
from sources.base import fill_from_page, looks_like_funding

IZVOR = "mzom"
DAVATELJ = "Ministarstvo znanosti, obrazovanja i mladih"
METODA = "statični HTML, paginacija"
BASE = "https://mzom.gov.hr"
LIST = BASE + "/natjecaji-196/196?trazi=1&tip=&tip2=&tema=&datumod=&datumdo=&pojam=&page={page}"
NE_TIPOVI = ("zapošljavanj", "oglas", "nabav")


def fetch_list() -> list[RawItem]:
    http = get_client()
    items: dict[str, RawItem] = {}
    preskoceno = 0
    total = None
    for page in range(1, config.MAX_LIST_PAGES + 1):
        html = http.get_text(LIST.format(page=page))
        if total is None and (m := re.search(r"Pronađeno (\d+)", html)):
            total = int(m.group(1))
        tree = LexborHTMLParser(html)
        divs = tree.css("div.news_item")
        if not divs:
            break
        for div in divs:
            a = div.css_first("a")
            href = a.attributes.get("href", "")
            nid = href.rstrip("/").rsplit("/", 1)[-1]
            naziv = clean(a.text())
            date_span = div.css_first("span.date")
            parts = [clean(p) for p in clean(date_span.text()).split("|")] if date_span else []
            tip = parts[1] if len(parts) > 1 else ""
            if not looks_like_funding(naziv) or any(t in tip.lower() for t in NE_TIPOVI):
                preskoceno += 1
                continue
            rok = re.search(r"Rok:\s*(.+?)(?:\s{2}|$)", clean(div.text()).replace(clean(date_span.text()) if date_span else "", ""))
            items[f"{IZVOR}-{nid}"] = RawItem(
                izvor=IZVOR, id=f"{IZVOR}-{nid}", url=BASE + href, naziv=naziv, davatelj=DAVATELJ,
                datum_objave=first_date(parts[0]) if parts else None,
                rok_hint=first_date(rok.group(1)) if rok else None,
                vrsta_hint="stipendija" if "stipendij" in naziv.lower() else None,
                kategorije=parts[1:], zupanije_hint=["sve"],
            )
    out = list(items.values())
    if out:
        out[0].extra.update({"ukupno_na_izvoru": total, "preskoceno_nije_financiranje": preskoceno})
    return out


def fetch_detail(item: RawItem) -> RawItem:
    return fill_from_page(item, (".page_content", ".article_left", "main"))
