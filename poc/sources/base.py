"""Pomoćne funkcije zajedničke izvorima."""
import re
from urllib.parse import urljoin, urlsplit, unquote

from selectolax.lexbor import LexborHTMLParser

from common.http import get_client
from common.models import RawItem
from common.text import clean, html_to_text

ATTACH_RE = re.compile(r"\.(pdf|docx?|xlsx?|odt|zip)(\?|$)", re.I)


def attachments(node, base_url: str) -> list[dict]:
    out, seen = [], set()
    for a in node.css("a[href]"):
        href = a.attributes.get("href") or ""
        if not ATTACH_RE.search(href):
            continue
        url = urljoin(base_url, href.strip())
        if url in seen:
            continue
        seen.add(url)
        naziv = clean(a.text()) or unquote(urlsplit(url).path.rsplit("/", 1)[-1])
        out.append({"naziv": naziv[:200], "url": url})
    return out


def fill_from_page(item: RawItem, selectors: tuple[str, ...], max_chars: int = 30000) -> RawItem:
    """Generički skupljač detalja: tekst glavnog sadržaja + prilozi."""
    html = get_client().get_text(item.url)
    tree = LexborHTMLParser(html)
    node = None
    for sel in selectors:
        node = tree.css_first(sel)
        if node is not None:
            break
    node = node or tree.body
    item.prilozi = item.prilozi or attachments(node, item.url)
    item.tekst = html_to_text(node)[:max_chars]
    item.detalji_dohvaceni = True
    return item


def is_pdf(att: dict) -> bool:
    return bool(re.search(r"\.pdf(\?|$)", att["url"], re.I))


# naslovi koji nisu natječaji za financiranje (zapošljavanje, javna nabava, nekretnine...)
NOT_FUNDING_RE = re.compile(
    r"radn(o|a|i|om) mjest|prijam u (radni odnos|službu)|zapošljavanj[ea] (u|na)|oglas za prijam|"
    r"javn[ai] nabav|istraživanj[ea] tržišta|zakup|prodaj[ua] |kupnj[ua] nekretnin|savjetovanj|"
    r"izbor (na|člana|kandidat)|imenovanj[ea] (ravnatelj|pročelni|člana)|ocjenjivač|vanjsk(e|ih) stručnjak|"
    r"rješenje o|obavijest o (provjeri|terminu|rezultatima testiranja)|provjer[ae] znanja|"
    r"poništenj|obustav|prinudnog upravitelja|upravitelj[ae] zgrad|natječaj za radna mjesta|"
    r"lektor|javnog dobra|preimenovanj|regulacij|parkiranj|davanj[ea] na korištenje|površin[ae] javne|"
    r"prikupljanj[ea] ponuda|dozvol[ae]|pomorsk(o|om) dobr|koncesij|najam|iznajmljivanj|stanova|nekretnin|"
    r"javn(o|a) priznanj|kreditn(e|a) ocjen|odgod[ua] plaćanja|rezultati|ravnatelj|proslavi|izabran|konzultant",
    re.I,
)
FUNDING_RE = re.compile(
    r"natje[čc]aj|javn(i|og) poziv|poziv (na|za)|potpor|poticaj|sufinanc|financiranj|stipendij|"
    r"subvencij|bespovratn|vaučer|vaucer|nagrad|zajam|kredit|jamstv|mjer[ae]",
    re.I,
)


def looks_like_funding(title: str) -> bool:
    return bool(FUNDING_RE.search(title)) and not NOT_FUNDING_RE.search(title)
