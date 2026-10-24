"""Ured za udruge Vlade RH: središnji pregled natječaja za udruge (sva tijela i JLS).

HTML s paginacijom i filtrom statusa: `natjecaji-5503/5503?trazi=1&status=1|2&page=N`.
Svaka stavka ima naziv, davatelja, datum, vrstu davatelja i status. Ukupno ~3.300
stavki od 2015., pa se otvoreni dohvaćaju svi, a zatvoreni samo prvih N stranica.
"""
import re

from selectolax.lexbor import LexborHTMLParser

import config
from common.http import get_client
from common.models import RawItem
from common.text import clean, first_date
from sources.base import fill_from_page

IZVOR = "udruge"
METODA = "statični HTML, paginacija + filtar statusa"
BASE = "https://udruge.gov.hr"
LIST = BASE + "/istaknute-teme/financiranje-programa-i-projekata-udruga-iz-javnih-izvora/natjecaji-5503/5503"


def _page(status: int, page: int) -> tuple[list[RawItem], int | None]:
    url = f"{LIST}?trazi=1&davatelj=&podrucje=&status={status}&datumod=&datumdo=&pojam=&page={page}"
    html = get_client().get_text(url)
    tree = LexborHTMLParser(html)
    total = re.search(r"Pronađeno (\d+) rezultata", html)
    out = []
    for div in tree.css("div.news_item"):
        a = div.css_first("a")
        nid = re.search(r"nid=(\d+)", a.attributes.get("href", ""))
        if not nid:
            continue
        date_span = div.css_first("span.date")
        parts = [clean(p) for p in clean(date_span.text()).split("|")] if date_span else []
        davatelj = clean(div.text())
        davatelj = davatelj.replace(clean(a.text()), "", 1).replace(clean(date_span.text()) if date_span else "", "").strip()
        st = (parts[2].lower() if len(parts) > 2 else "")
        out.append(RawItem(
            izvor=IZVOR, id=f"{IZVOR}-{nid.group(1)}",
            url=f"{BASE}/natjecaji-5503/5503?nid={nid.group(1)}", naziv=clean(a.text()),
            davatelj=davatelj or None, datum_objave=first_date(parts[0]) if parts else None,
            status_hint="otvoren" if "otvoren" in st else ("zatvoren" if "zatvoren" in st else None),
            kategorije=[parts[1]] if len(parts) > 1 else [],
            extra={"vrsta_davatelja": parts[1] if len(parts) > 1 else None},
        ))
    return out, int(total.group(1)) if total else None


def fetch_list() -> list[RawItem]:
    items: dict[str, RawItem] = {}
    totals = {}
    for status, max_pages in ((1, 60), (2, config.MAX_LIST_PAGES)):
        for page in range(1, max_pages + 1):
            batch, total = _page(status, page)
            totals.setdefault(status, total)
            new = [i for i in batch if i.id not in items]
            if not new:
                break
            for i in new:
                items[i.id] = i
    out = list(items.values())
    if out:
        out[0].extra["ukupno_na_izvoru"] = {"otvoreni": totals.get(1), "zatvoreni": totals.get(2)}
    return out


def fetch_detail(item: RawItem) -> RawItem:
    fill_from_page(item, (".page_content", ".article_left", "main"))
    # detalj je obrazac "Oznaka:\nvrijednost" -> strukturirana polja bez LLM-a
    kv = dict(re.findall(r"^([^\n:]{3,80}):\n([^\n]+)", item.tekst, re.M))
    polja = {}
    if (z := kv.get("Područje provedbe aktivnosti (županija)")):
        item.zupanije_hint = [clean(x) for x in re.split(r",|;", z) if clean(x)]
    if (d := kv.get("Datum zatvaranja natječaja/poziva")):
        item.rok_hint = first_date(d)
    for key, field in (("Ukupni iznos raspoloživih sredstava", "iznos_ukupno_eur"),
                       ("Najniži iznos sredstava za financiranje programa/projekta", "iznos_po_korisniku_min_eur"),
                       ("Najviši iznos sredstava za financiranje programa/projekta", "iznos_po_korisniku_max_eur")):
        if kv.get(key):
            polja[field] = kv[key]
    item.extra["polja"] = polja
    link = LexborHTMLParser(get_client().get_text(item.url)).css_first(".page_content a[href^=http]")
    if link is not None:
        item.extra["izvorni_link"] = link.attributes.get("href")
    return item
