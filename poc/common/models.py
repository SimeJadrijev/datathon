"""Zajedničko sučelje izvora: RawItem (sirovi zapis) i ciljna shema natječaja."""
from dataclasses import dataclass, field

VRSTE = ["poticaj", "natječaj", "javni poziv", "stipendija", "zajam", "nagrada"]
STATUSI = ["najavljen", "otvoren", "zatvoren"]
CILJNE_SKUPINE = ["gradjanin", "obrt", "doo", "udruga", "jls", "student", "poljoprivrednik"]
PODRUCJA = [
    "okolis", "energetika", "promet", "poljoprivreda", "obrazovanje", "znanost",
    "kultura", "sport", "socijala", "zdravstvo", "zaposljavanje", "poduzetnistvo",
    "inovacije", "digitalizacija", "turizam", "mladi", "civilno_drustvo",
]

POLJA = [
    "id", "izvor", "url", "naziv", "davatelj", "vrsta", "status", "datum_objave",
    "rok_prijave", "iznos_ukupno_eur", "iznos_po_korisniku_min_eur",
    "iznos_po_korisniku_max_eur", "ciljne_skupine", "podrucja", "nkd_sifre",
    "zupanije", "uvjeti_sazetak", "potrebni_dokumenti", "prilozi",
    "sirovi_tekst_hash", "izvuceno_llm", "dohvaceno",
]
LIST_POLJA = {"ciljne_skupine", "podrucja", "nkd_sifre", "zupanije", "potrebni_dokumenti", "prilozi"}


@dataclass
class RawItem:
    """Ono što izvor zna o natječaju prije izvlačenja polja.

    Polja *_hint dolaze izravno iz strukture izvora (npr. status iz CSS klase,
    datum iz RSS-a). Pravila i LLM ih koriste kao jače dokaze od slobodnog teksta.
    """
    izvor: str
    id: str
    url: str
    naziv: str
    davatelj: str | None = None
    datum_objave: str | None = None   # ISO
    status_hint: str | None = None
    rok_hint: str | None = None       # ISO
    vrsta_hint: str | None = None
    kategorije: list[str] = field(default_factory=list)
    zupanije_hint: list[str] = field(default_factory=list)
    tekst: str = ""                   # tekst stranice detalja (bez navigacije)
    prilozi: list[dict] = field(default_factory=list)   # {"naziv", "url"}
    pdf_tekst: str = ""               # spojen tekst preuzetih PDF-ova
    pdf_preuzeto: int = 0
    detalji_dohvaceni: bool = False
    extra: dict = field(default_factory=dict)
