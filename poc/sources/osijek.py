"""Grad Osijek.

WordPress s javnim REST API-jem i vlastitim tipom `natjecaji` (~1.000 zapisa od 2017.),
ali tip miješa natječaje, savjetovanja, oglase za posao i prodaju nekretnina,
pa se filtrira po naslovu. Tekst i prilozi su u ACF poljima (`acf_fields.sadrzaj`, `datoteka`, `datoteke`),
pa stranicu objave nije potrebno dohvaćati; natječaj je najčešće samo PDF.
"""
import httpx
from selectolax.lexbor import LexborHTMLParser

import config
from common.http import get_client
from common.models import RawItem
from common.text import clean, html_to_text
from sources.base import looks_like_funding

IZVOR = "osijek"
DAVATELJ = "Grad Osijek"
METODA = "WordPress REST API (custom post type `natjecaji`)"
API = "https://www.osijek.hr/wp-json/wp/v2/natjecaji"


def fetch_list() -> list[RawItem]:
    http = get_client()
    items = []
    ukupno = preskoceno = 0
    for page in range(1, min(config.MAX_LIST_PAGES, 11) + 1):
        try:
            batch = http.get_json(f"{API}?per_page=100&page={page}&_fields=id,date,link,title,acf_fields")
        except httpx.HTTPStatusError:
            break
        for p in batch:
            ukupno += 1
            naziv = clean(p["title"]["rendered"])
            if not looks_like_funding(naziv):
                preskoceno += 1
                continue
            items.append(RawItem(
                izvor=IZVOR, id=f"{IZVOR}-{p['id']}", url=p["link"], naziv=naziv, davatelj=DAVATELJ,
                datum_objave=p["date"][:10], zupanije_hint=["Osječko-baranjska"],
                vrsta_hint="stipendija" if "stipendij" in naziv.lower() else None,
                tekst=html_to_text(LexborHTMLParser((p.get("acf_fields") or {}).get("sadrzaj") or "<p></p>").body),
                prilozi=_files(p.get("acf_fields") or {}), detalji_dohvaceni=True,
            ))
        if len(batch) < 100:
            break
    if items:
        items[0].extra.update({"ukupno_na_izvoru": ukupno, "preskoceno_nije_financiranje": preskoceno})
    return items


def _files(acf) -> list[dict]:
    """Rekurzivno skupi sve datoteke iz ACF polja (struktura varira među objavama)."""
    out = []
    if isinstance(acf, dict):
        if acf.get("url") and (acf.get("filename") or acf.get("mime_type")):
            out.append({"naziv": acf.get("title") or acf.get("filename"), "url": acf["url"]})
        else:
            for v in acf.values():
                out += _files(v)
    elif isinstance(acf, list):
        for v in acf:
            out += _files(v)
    return out


def fetch_detail(item: RawItem) -> RawItem:
    return item          # sve je već u odgovoru REST API-ja
