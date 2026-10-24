"""Izvlačenje polja pravilima i regexom (bez LLM-a).

Redoslijed dokaza: strukturirani podatak izvora (hint) > tekst stranice > tekst PDF-a.
Polja koja se ne mogu pouzdano izvući pravilima (uvjeti, dokumenti) ostaju null.
"""
import hashlib
import re
from datetime import date, datetime

from common.models import RawItem
from common.text import first_date, parse_dates

TODAY = date.today().isoformat()

# --- iznosi -----------------------------------------------------------------
AMOUNT_RE = re.compile(
    r"(?<![\d.,])(\d{1,3}(?:[.\s]\d{3})+|\d+)(?:,(\d{1,2}))?\s*"
    r"(milijuna|milijun|mil\.|mln\.?|tisuća)?\s*(?:eura|eur\b|€)",
    re.IGNORECASE,
)
AMOUNT_PREFIX_RE = re.compile(r"(?:€|EUR)\s*(\d{1,3}(?:\.\d{3})+|\d+)(?:,(\d{1,2}))?", re.IGNORECASE)
CTX_TOTAL = re.compile(r"ukupn|raspoloživ|alokacij|iznos sredstava|vrijednost (?:poziva|natječaja)|osiguran[ao]? (?:je|su)? ?u|proračun", re.I)
CTX_MAX = re.compile(r"najviš|maksimaln|najveć|ne može (?:biti )?već|gornj|do iznosa|do najviše", re.I)
CTX_MIN = re.compile(r"najniž|minimaln|najmanj|ne može (?:biti )?manj|donj", re.I)
CTX_PER = re.compile(r"po (?:korisniku|prijavitelju|projektu|zahtjevu|osobi|studentu|učeniku|vozilu|kućanstvu)|mjesečn|stipendij[ae] (?:iznosi|u iznosu)|u iznosu od", re.I)


def parse_amount(num: str, dec: str | None, mult: str | None) -> float | None:
    try:
        v = float(re.sub(r"[.\s]", "", num) + ("." + dec if dec else ""))
    except ValueError:
        return None
    if mult:
        m = mult.lower()
        v *= 1_000_000 if m.startswith(("mil", "mln")) else 1_000
    return v


def amounts(text: str) -> list[tuple[str, float]]:
    """Vraća [(uloga, iznos)], uloga ∈ total|max|min|per|?, prema kontekstu prije iznosa."""
    out = []
    for m in AMOUNT_RE.finditer(text):
        v = parse_amount(*m.groups())
        if v is None or v < 50:
            continue
        ctx = text[max(0, m.start() - 140): m.start()]
        # najbliža ključna riječ odlučuje (zadnja pojava u kontekstu)
        best, pos = "?", -1
        for role, rx in (("total", CTX_TOTAL), ("max", CTX_MAX), ("min", CTX_MIN), ("per", CTX_PER)):
            for k in rx.finditer(ctx):
                if k.start() > pos:
                    best, pos = role, k.start()
        out.append((best, v))
    return out


def pick_amounts(texts: list[str]) -> dict:
    res = {"iznos_ukupno_eur": None, "iznos_po_korisniku_min_eur": None, "iznos_po_korisniku_max_eur": None}
    for t in texts:
        for role, v in amounts(t):
            if role == "total" and res["iznos_ukupno_eur"] is None:
                res["iznos_ukupno_eur"] = v
            elif role in ("max", "per") and res["iznos_po_korisniku_max_eur"] is None:
                res["iznos_po_korisniku_max_eur"] = v
            elif role == "min" and res["iznos_po_korisniku_min_eur"] is None:
                res["iznos_po_korisniku_min_eur"] = v
    # sanity: iznos po korisniku ne može biti veći od ukupnog
    if res["iznos_ukupno_eur"] is not None and res["iznos_ukupno_eur"] < 1000:
        res["iznos_ukupno_eur"] = None        # "ukupno" uz mali iznos je gotovo uvijek nešto drugo
    tot, mx = res["iznos_ukupno_eur"], res["iznos_po_korisniku_max_eur"]
    if tot and mx and mx > tot:
        res["iznos_ukupno_eur"], res["iznos_po_korisniku_max_eur"] = mx, tot
    return res


def struct_amount(s: str | None) -> float | None:
    if not s:
        return None
    m = AMOUNT_RE.search(s) or AMOUNT_PREFIX_RE.search(s)
    if m:
        g = m.groups()
        return parse_amount(g[0], g[1], g[2] if len(g) > 2 else None)
    m = re.search(r"(\d{1,3}(?:\.\d{3})+|\d+)(?:,(\d{1,2}))?", s)
    return parse_amount(m.group(1), m.group(2), None) if m else None


# --- rok --------------------------------------------------------------------
ROK_RE = re.compile(
    r"rok(?:a|om)?\s+(?:je\s+)?(?:za\s+)?(?:podnošenj|prijav|dostav|predaj|zaprimanj)|rok:|"
    r"zaključno\s+(?:s|do|sa)|najkasnije\s+do|otvoren\s+do|traje\s+do|"
    r"(?:prijave|zahtjevi|prijedlozi)\s+(?:se\s+)?(?:podnose|zaprimaju|dostavljaju)[^.]{0,80}?\s+do|"
    r"datum zatvaranja|do\s+(?:dana\s+)?(?=\d{1,2}\.\s?(?:\d{1,2}\.|[a-zčćšđž]+\s+\d{4}))",
    re.IGNORECASE,
)


def find_rok(text: str, objava: str | None) -> str | None:
    for m in ROK_RE.finditer(text):
        window = text[m.start(): m.end() + 160]
        for _, d in parse_dates(window):
            if objava and d <= objava:        # rok na dan objave je gotovo uvijek pogrešno uhvaćen datum objave
                continue
            return d
    return None


# --- klasifikacija ključnim riječima -----------------------------------------
SKUPINE_KW = {
    "student": r"student|učenik|redovit\w* status|akademsk\w* godin|stipendij",
    "gradjanin": r"fizičk\w* osob|građan|kućanstv|obitelj\w* kuć|nezaposlen|roditelj",
    "obrt": r"\bobrt|samostaln\w* djelatnost",
    "doo": r"trgovačk\w* društv|d\.o\.o|poduzetni|poslodav|mikro, mal\w* i srednj|\bMSP\b|malog gospodarstva",
    "udruga": r"udrug|organizacij\w* civilnog|neprofitn|OCD\b|zaklad",
    "jls": r"jedinic\w* lokaln|\bJLS\b|\bJL\(R\)S|općin\w* i grad|gradov\w* i općin|županij\w* (?:mogu|kao prijavitelj)|javn\w* ustanov",
    "poljoprivrednik": r"poljoprivredn\w* gospodarstv|\bOPG|poljoprivrednik|ribar|primarn\w* poljoprivredn",
}
PODRUCJA_KW = {
    "okolis": r"okoliš|otpad|klimat|bioraznolik|priroda|kompost|ekološk|zelen",
    "energetika": r"energet|obnovljiv|fotonapon|solarn|toplinsk\w* crpk|kondenzacijsk|emisij",
    "promet": r"vozil|promet|mobilnost|e-romobil|bicikl|punionic",
    "poljoprivreda": r"poljopriv|ruraln|ribarst|stočar|sjemen|pčel|šumarst|agro",
    "obrazovanje": r"obrazovanj|škol|student|učenik|stipendij|udžbenik|izobrazb|osposobljavanj",
    "znanost": r"znanost|istraživ|znanstven|akademsk",
    "kultura": r"kultur|umjetnost|baštin|knjižn|muzej|kazališ",
    "sport": r"\bsport|tjelesn\w* aktivnost",
    "socijala": r"socijal|siromaš|invaliditet|beskućn|starij\w* osob|ranjiv",
    "zdravstvo": r"zdravstv|zdravlj|ovisnost|bolest",
    "zaposljavanje": r"zapošljavanj|samozapošljavanj|nezaposlen|radn\w* mjest|pripravništv",
    "poduzetnistvo": r"poduzet|obrtni|gospodarsk|investicij|mal\w* gospodarstv|jamstv|zajam|kredit",
    "inovacije": r"inovac|startup|start-up|razvoj proizvoda|pametn\w* specijalizacij|S3",
    "digitalizacija": r"digital",
    "turizam": r"turiz",
    "mladi": r"\bmlad(i|ih|ima)\b|mladež",
    "civilno_drustvo": r"udrug|civiln\w* društv|volonter",
}
SKUPINE_RX = {k: re.compile(v, re.I) for k, v in SKUPINE_KW.items()}
PODRUCJA_RX = {k: re.compile(v, re.I) for k, v in PODRUCJA_KW.items()}

IZVOR_SKUPINE = {"udruge": ["udruga"], "ampeu": ["student"]}
HZZ_KAT = {"Poslodavac": ["doo", "obrt"], "Nezaposlen": ["gradjanin"], "Samozapošljavanje": ["gradjanin"],
           "Zaposlen": ["gradjanin"]}


def classify(rx: dict, text: str, min_hits: int = 1) -> list[str]:
    return [k for k, r in rx.items() if len(r.findall(text)) >= min_hits]


def vrsta(item: RawItem) -> str | None:
    if item.vrsta_hint:
        return item.vrsta_hint
    t = item.naziv.lower()
    for kw, v in (("stipendij", "stipendija"), ("zajam", "zajam"), ("zajmov", "zajam"), ("kredit", "zajam"),
                  ("jamstv", "zajam"), ("nagrad", "nagrada"), ("potpor", "poticaj"), ("poticaj", "poticaj"),
                  ("sufinanc", "poticaj"), ("subvencij", "poticaj"), ("vaučer", "poticaj"),
                  ("javni poziv", "javni poziv"), ("poziv", "javni poziv"), ("natječaj", "natječaj"),
                  ("natjecaj", "natječaj"), ("mjer", "poticaj")):
        if kw in t:
            return v
    return None


def status(item: RawItem, rok: str | None) -> str | None:
    if item.status_hint:
        return item.status_hint
    head = (item.naziv + " " + item.tekst[:400]).lower()
    if "najav" in item.naziv.lower():
        return "najavljen"
    if "zatvoreno" in head or "zatvoren je" in head:
        return "zatvoren"
    if rok:
        return "otvoren" if rok >= TODAY else "zatvoren"
    return None


def text_hash(item: RawItem) -> str:
    return hashlib.sha256((item.naziv + "\n" + item.tekst + "\n" + item.pdf_tekst).encode()).hexdigest()[:16]


def extract(item: RawItem) -> dict:
    page = f"{item.naziv}\n{item.tekst}"
    full = page + "\n" + item.pdf_tekst
    objava = item.datum_objave or first_date(item.tekst[:400])
    rok = item.rok_hint or find_rok(page, objava) or find_rok(item.pdf_tekst, objava)

    polja = item.extra.get("polja", {})
    iznosi = pick_amounts([page, item.pdf_tekst])
    for k, v in polja.items():          # strukturirani iznos s izvora ima prednost
        if (a := struct_amount(v)) is not None:
            iznosi[k] = a

    skupine = set(IZVOR_SKUPINE.get(item.izvor, []))
    for k in item.kategorije:
        skupine.update(HZZ_KAT.get(k, []))
    # klasifikacija na tekstu stranice; PDF samo kad stranica nema sadržaja (PDF-ovi nose puno šuma)
    body = item.tekst if len(item.tekst) > 500 else full
    skupine.update(classify(SKUPINE_RX, item.naziv + "\n" + body[:5000]))
    podrucja = classify(PODRUCJA_RX, item.naziv + " " + " ".join(item.kategorije))
    podrucja = sorted(set(podrucja) | set(classify(PODRUCJA_RX, body[:5000], min_hits=3)))

    nkd = sorted(set(re.findall(r"\b(\d{2}\.\d{1,2})\b", " ".join(
        full[m.start(): m.start() + 300] for m in re.finditer(r"\bNKD\b", full)))))

    return {
        "id": item.id, "izvor": item.izvor, "url": item.url, "naziv": item.naziv,
        "davatelj": item.davatelj, "vrsta": vrsta(item), "status": status(item, rok),
        "datum_objave": objava, "rok_prijave": rok, **iznosi,
        "ciljne_skupine": sorted(skupine) or None, "podrucja": podrucja or None,
        "nkd_sifre": nkd or None, "zupanije": item.zupanije_hint or None,
        "uvjeti_sazetak": None, "potrebni_dokumenti": None,
        "prilozi": [{"naziv": p["naziv"], "url": p["url"]} for p in item.prilozi] or None,
        "sirovi_tekst_hash": text_hash(item), "izvuceno_llm": False,
        "dohvaceno": datetime.now().isoformat(timespec="seconds"),
    }
