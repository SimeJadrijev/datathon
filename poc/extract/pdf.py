"""Preuzimanje i čitanje PDF priloga (najviše MAX_PDF_PER_ITEM po natječaju)."""
import io
import logging
import re

import pdfplumber

import config
from common.http import Blocked, get_client
from common.models import RawItem
from sources.base import is_pdf

logging.getLogger("pdfminer").setLevel(logging.ERROR)

# prednost dokumentima koji vjerojatno sadrže uvjete (tekst poziva), a ne obrascima
PRIORITET = re.compile(r"javni poziv|natje[čc]aj|poziv|uput|pravilnik|program", re.I)
NISKI = re.compile(r"obrazac|izjava|prilog \d|tablica|odluka o odabiru|rezultat|pitanja", re.I)


def pick(prilozi: list[dict]) -> list[dict]:
    pdfs = [p for p in prilozi if is_pdf(p)]
    pdfs.sort(key=lambda p: (not PRIORITET.search(p["naziv"]), bool(NISKI.search(p["naziv"]))))
    return pdfs[: config.MAX_PDF_PER_ITEM]


def pdf_text(data: bytes, max_pages: int = config.MAX_PDF_PAGES) -> str:
    try:
        with pdfplumber.open(io.BytesIO(data)) as pdf:
            pages = [(p.extract_text() or "") for p in pdf.pages[:max_pages]]
        return "\n".join(pages)
    except Exception as e:          # pokvaren ili skeniran PDF
        return f"[PDF nečitljiv: {type(e).__name__}]"


def download(item: RawItem) -> tuple[int, list[str]]:
    """Preuzme do 3 PDF-a i spoji njihov tekst u item.pdf_tekst. Vraća (broj, greške)."""
    http = get_client()
    texts, errors = [], []
    for p in pick(item.prilozi):
        try:
            data = http.get_pdf(p["url"])
        except Blocked:
            errors.append(f"robots.txt: {p['url']}")
            continue
        except Exception as e:
            errors.append(f"{type(e).__name__}: {p['url'][:100]}")
            continue
        t = pdf_text(data)
        p["znakova_teksta"] = len(t)
        texts.append(f"=== {p['naziv']} ===\n{t}")
    item.pdf_tekst = "\n\n".join(texts)
    item.pdf_preuzeto = len(texts)
    return len(texts), errors
