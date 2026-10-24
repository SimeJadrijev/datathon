"""Narodne novine, službeni dio: novi zakoni i drugi propisi.

Pretraga službenog dijela po godini (`search.aspx?kategorija=1&godina=G&rpp=200&str=N`)
vraća 200 akata po stranici: naslov, broj NN, broj dokumenta, vrsta (zakon, uredba,
pravilnik, odluka...) i datum. Svaki akt ima stalni ELI identifikator
(`/eli/sluzbeni/<godina>/<broj NN>/<broj dokumenta>`), a stranica akta nosi ELI
metapodatke (date_document, date_publication, type_document) kao RDFa.
robots.txt zabranjuje oglasni dio i pojedine stare akte; službeni popis je dopušten.
"""
import re
from datetime import datetime

from selectolax.lexbor import LexborHTMLParser

import config
from common.http import Blocked, get_client
from common.text import clean, first_date
from extract.rules import PODRUCJA_RX, classify

BASE = "https://narodne-novine.nn.hr"
SEARCH = BASE + "/search.aspx?sortiraj=4&kategorija=1&godina={godina}&rpp=200&qtype=1&pretraga=da&str={str}"
META_RE = re.compile(r'property="http://data\.europa\.eu/eli/ontology#(\w+)"\s+(?:content|resource)="([^"]+)"')
INFO_RE = re.compile(r"NN\s+(\d+)/(\d{4}),\s*\((\d+)\),\s*([^,]+),\s*([\d.]+)")


def fetch_list(godina: int | None = None, max_pages: int = 10) -> list[dict]:
    godina = godina or datetime.now().year
    http = get_client()
    out: dict[str, dict] = {}
    for page in range(0, max_pages):          # parametar str je indeks stranice od 0
        try:
            html = http.get_text(SEARCH.format(godina=godina, str=page))
        except Blocked:
            break
        items = LexborHTMLParser(html).css(".searchListItem")
        new = 0
        for it in items:
            a = it.css_first(".resultTitle a")
            if a is None:
                continue
            for score in a.css("span.score"):       # skriveni broj relevantnosti ("0") nije dio naslova
                score.decompose()
            naziv = clean(a.text())
            m = INFO_RE.search(clean(it.text()))
            if not m:
                continue
            broj, god, dok, vrsta, datum = m.groups()
            pid = f"nn-{god}-{broj}-{dok}"
            if pid in out:
                continue
            new += 1
            out[pid] = {
                "id": pid, "naziv": naziv, "vrsta": clean(vrsta).lower(),
                "nn_broj": f"{broj}/{god}", "dokument": int(dok),
                "datum_objave": first_date(datum), "datum_dokumenta": None,
                "url": BASE + a.attributes["href"],
                "eli": f"{BASE}/eli/sluzbeni/{god}/{broj}/{dok}",
                "pdf": f"{BASE}/eli/sluzbeni/{god}/{broj}/{dok}/pdf",
                "podrucja": classify(PODRUCJA_RX, naziv) or None,
                "eli_meta": False,
            }
        if new == 0 or len(items) < 200:
            break
    return list(out.values())


def fetch_eli(akt: dict) -> dict:
    """Dopuni službenim ELI metapodacima sa stranice akta (datum donošenja, vrsta)."""
    html = get_client().get_text(akt["url"], ttl_h=24 * 365)    # objavljeni akt se ne mijenja
    meta = {}
    for k, v in META_RE.findall(html):
        meta.setdefault(k, v)
    if meta.get("date_document"):
        akt["datum_dokumenta"] = meta["date_document"]
    if meta.get("date_publication"):
        akt["datum_objave"] = meta["date_publication"]
    if meta.get("type_document"):
        akt["vrsta_eli"] = meta["type_document"].rsplit("/", 1)[-1].lower()
    akt["eli_meta"] = bool(meta)
    return akt


def run(max_eli: int = 60) -> list[dict]:
    """Popis tekuće i prošle godine + ELI metapodaci za najnovije zakone."""
    now = datetime.now().year
    akti = fetch_list(now) + fetch_list(now - 1, max_pages=config.MAX_LIST_PAGES)
    zakoni = sorted((a for a in akti if a["vrsta"] == "zakon"), key=lambda a: a["datum_objave"] or "", reverse=True)
    for a in zakoni[:max_eli]:
        try:
            fetch_eli(a)
        except Exception:
            pass
    return akti
