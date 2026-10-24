"""FZOEU (Fond za zaštitu okoliša i energetsku učinkovitost).

Stranica "Nacionalni javni pozivi i natječaji" sadrži sve pozive kao accordion:
naslov, status (CSS klasa status-N + tekst), datum otvaranja, opis i dokumente.
Zadani URL iz upute (/hr/natjecaji/7) preusmjerava na stranicu jednog programa.
"""
import re

from selectolax.lexbor import LexborHTMLParser

from common.http import get_client
from common.models import RawItem
from common.text import clean, first_date, html_to_text
from sources.base import attachments

IZVOR = "fzoeu"
DAVATELJ = "Fond za zaštitu okoliša i energetsku učinkovitost"
METODA = "statični HTML (jedna stranica, accordion)"
URL = "https://www.fzoeu.hr/hr/nacionalni-javni-pozivi-i-natjecaji/1367"


def fetch_list() -> list[RawItem]:
    tree = LexborHTMLParser(get_client().get_text(URL))
    items = []
    for header in tree.css("li.accordion-header"):
        body = header.next
        while body is not None and body.tag != "li":
            body = body.next
        if body is None:
            continue
        status_node = header.css_first(".status span")
        status = clean(status_node.text()).lower() if status_node else None
        title_divs = [d for d in header.css("div > div") if not d.css_first(".status")]
        naziv = clean(title_divs[-1].text()) if title_divs else clean(header.text())
        if status and naziv.lower().startswith(status):
            naziv = naziv[len(status):].strip()
        prilozi = attachments(body, URL)
        m = re.search(r"/docs/(\d+)/", " ".join(p["url"] for p in prilozi))
        oznaka = re.search(r"\(([A-Za-z]+-?\d+/\d+[^)]*)\)", naziv)
        key = m.group(1) if m else (oznaka.group(1) if oznaka else str(abs(hash(naziv)) % 10**8))
        tekst = html_to_text(body)
        otvoren = re.search(r"Otvoren:\s*([\d.\s]+)", tekst)
        items.append(RawItem(
            izvor=IZVOR, id=f"{IZVOR}-{key.replace('/', '_')}", url=URL + f"#{key}", naziv=naziv,
            davatelj=DAVATELJ, status_hint=_status(status),
            datum_objave=first_date(otvoren.group(1)) if otvoren else None,
            zupanije_hint=["sve"], tekst=tekst, prilozi=prilozi, detalji_dohvaceni=True,
            extra={"status_tekst": status},
        ))
    return items


def _status(s: str | None) -> str | None:
    if not s:
        return None
    if "otvoren" in s:
        return "otvoren"
    if "najav" in s:
        return "najavljen"
    if "zatvoren" in s or "obustav" in s or "završen" in s:
        return "zatvoren"
    return None


def fetch_detail(item: RawItem) -> RawItem:
    return item          # sve je na stranici popisa
