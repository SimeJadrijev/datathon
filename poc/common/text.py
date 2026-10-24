"""Pomoćne funkcije za tekst: HTML -> tekst, hrvatski datumi."""
import html as htmlmod
import re
from datetime import date

from selectolax.lexbor import LexborHTMLParser

MJESECI = {
    "siječnja": 1, "sijecnja": 1, "veljače": 2, "veljace": 2, "ožujka": 3, "ozujka": 3,
    "travnja": 4, "svibnja": 5, "lipnja": 6, "srpnja": 7, "kolovoza": 8, "rujna": 9,
    "listopada": 10, "studenoga": 11, "studenog": 11, "prosinca": 12,
    "siječanj": 1, "veljača": 2, "ožujak": 3, "travanj": 4, "svibanj": 5, "lipanj": 6,
    "srpanj": 7, "kolovoz": 8, "rujan": 9, "listopad": 10, "studeni": 11, "prosinac": 12,
}
_MJ = "|".join(sorted(MJESECI, key=len, reverse=True))

# 28.10.2026. | 28. 10. 2026 | 31.12.26 | 28. listopada 2026.
DATE_RE = re.compile(
    rf"\b(\d{{1,2}})\.\s?(?:(\d{{1,2}})\.\s?|({_MJ})\s+)(\d{{4}}|\d{{2}})(?!\d)",
    re.IGNORECASE,
)


def parse_dates(text: str) -> list[tuple[int, str]]:
    """Svi datumi u tekstu kao (pozicija, ISO)."""
    out = []
    for m in DATE_RE.finditer(text or ""):
        d, mm, mjesec, y = m.groups()
        month = int(mm) if mm else MJESECI.get(mjesec.lower())
        year = int(y) + 2000 if len(y) == 2 else int(y)
        try:
            out.append((m.start(), date(year, month, int(d)).isoformat()))
        except (ValueError, TypeError):
            continue
    return out


def first_date(text: str) -> str | None:
    ds = parse_dates(text)
    return ds[0][1] if ds else None


def clean(s: str | None) -> str:
    return " ".join(htmlmod.unescape(s or "").split())


def html_to_text(html_or_node, selectors: tuple[str, ...] = ()) -> str:
    """Tekst glavnog sadržaja. Ako je dan popis selektora, uzima prvi koji postoji."""
    if isinstance(html_or_node, str):
        tree = LexborHTMLParser(html_or_node)
        node = None
        for sel in selectors:
            node = tree.css_first(sel)
            if node is not None:
                break
        node = node or tree.body or tree.root
    else:
        node = html_or_node
    if node is None:
        return ""
    for bad in node.css("script, style, nav, noscript, form"):
        bad.decompose()
    # zadrži prijelome redaka između blokova, lakše za regex i LLM
    txt = node.text(separator="\n")
    lines = [" ".join(l.split()) for l in txt.splitlines()]
    return "\n".join(l for l in lines if l)


def strip_tags(s: str) -> str:
    return clean(re.sub(r"<[^>]+>", " ", s or ""))
