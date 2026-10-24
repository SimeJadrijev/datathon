# POC: agregator natječaja, poticaja i stipendija

Test izvedivosti za izazov C (Datathon 2026): koliko se natječaja može pouzdano skupiti s hrvatskih izvora, koja polja se mogu izvući, radi li podudaranje s profilom (i preko OIB-a). Nije aplikacija. Rezultat je [IZVJESTAJ.md](IZVJESTAJ.md).

## Pokretanje

Python 3.11+.

```bash
cd poc
python -m venv .venv
.venv/Scripts/activate          # Linux/macOS: source .venv/bin/activate
pip install -r requirements.txt
python run_all.py               # svi izvori -> data/natjecaji.sqlite -> IZVJESTAJ.md (prvi put ~10 min)
```

Prvo pokretanje traje oko 10 minuta jer se poštuje 1 zahtjev/s po domeni. Sljedeća pokretanja koriste cache iz `data/cache/` (HTML 24 h, PDF trajno).

Opcionalno, ključevi i postavke idu u `poc/.env` (predložak je `.env.example`; `.env` je u `.gitignore`, ne commitati ga):

```bash
cp .env.example .env            # pa upiši ključeve u .env
```

Varijable postavljene u okruženju imaju prednost pred `.env`.

| Varijabla | Učinak |
|---|---|
| `ANTHROPIC_API_KEY` | uključuje LLM izvlačenje (`claude-haiku-5-5`). Bez ključa se korak preskače, ostalo radi. |
| `SUDREG_CLIENT_ID`, `SUDREG_CLIENT_SECRET` | pravi Sudski registar umjesto mocka (`opendata/sudreg_mock.json`) |
| `POC_MAX_DETAILS`, `POC_MAX_PDF_ITEMS`, `POC_MAX_LIST_PAGES` | koliko detalja / PDF-ova / stranica popisa po izvoru (zadano 40 / 15 / 15) |
| `POC_LLM_BUDGET_USD` | gornja granica troška LLM-a (zadano 1.5) |

## Naredbe

| Naredba | Što radi |
|---|---|
| `python run_all.py` | sve: izvori, pravila, LLM na 5 natječaja, otvoreni podaci, podudaranje, izvještaj |
| `python run_all.py --llm-limit 300` | LLM na više natječaja (nakon provjere na 5) |
| `python run_all.py --izvori fzoeu,hzz` | samo odabrani izvori |
| `python run_all.py --refresh` | ponovno dohvati HTML (ignorira cache) |
| `python run_all.py --samo-izvjestaj` | samo ponovno generiraj `IZVJESTAJ.md` |
| `python match.py --svi-profili` | rangirani natječaji za 3 testna profila (`profiles/`) |
| `python match.py profiles/2_gradjanin_ev.json` | jedan profil |
| `python match.py --oib 00000000001` | profil iz Sudskog registra (bez ključa samo mock OIB-ovi) |
| `python notify_dry_run.py` | simulacija dva pokretanja i ispis obavijesti (ništa se ne šalje) |
| `python eval/make_sample.py` | `eval/uzorak.csv` sa 15 nasumičnih natječaja za ručnu provjeru |
| `python eval/usporedi.py` | točnost po polju nakon što tim ispuni stupce `tocno_*` |

## Pregled podataka u browseru

Baza se može pregledati i filtrirati u browseru preko [Datasette](https://datasette.io/) (samo alat za pregled, nije dio rješenja):

```bash
$env:PYTHONUTF8=1; .venv\Scripts\datasette data/natjecaji.sqlite -m datasette.yml -p 8001 --setting truncate_cells_html 300
```

Otvori http://localhost:8001. Spremljeni upiti (`datasette.yml`):

| URL | Što pokazuje |
|---|---|
| `/natjecaji/po_izvoru` | broj natječaja, otvorenih, s rokom, iznosom, PDF-om i LLM-om po izvoru |
| `/natjecaji/otvoreni` | otvoreni i najavljeni natječaji (filtar po izvoru) |
| `/natjecaji/pravila_vs_llm` | isti natječaj: rok, iznos i ciljne skupine iz pravila vs LLM-a, uz sažetak uvjeta i dokumente |
| `/natjecaji/llm_trosak` | tokeni i trošak po modelu |
| `/natjecaji/natjecaji` | cijela tablica s filtrima i pretragom |

Podudaranje profila se gleda u terminalu: `python match.py --svi-profili` ili `python match.py --oib <OIB>`.

## Struktura

| Mapa / datoteka | Sadržaj |
|---|---|
| `sources/` | jedan modul po izvoru, sučelje `fetch_list() -> list[RawItem]` i `fetch_detail(item) -> RawItem` |
| `common/` | pristojan HTTP klijent (robots.txt, 1 req/s po domeni, cache), `RawItem` i shema, SQLite, datumi |
| `extract/` | `rules.py` (regex: rok, iznosi, ciljne skupine, područja), `pdf.py` (do 3 PDF-a po natječaju), `llm.py` (Claude, structured output) |
| `opendata/` | `ckan.py` (data.gov.hr), `sudreg.py` (Sudski registar + mock), `nkd.py` (NKD -> područja) |
| `match.py`, `notify_dry_run.py` | podudaranje profila i simulacija obavijesti |
| `eval/` | uzorak za ručnu provjeru i izračun točnosti |
| `docs/izvori.md` | izviđanje: struktura svakog izvora, robots.txt, pristup |
| `docs/zakljucak.md` | ručno napisan zaključak koji se umeće u izvještaj |
| `data/` | cache i `natjecaji.sqlite` (u `.gitignore`) |

Baza: tablica `natjecaji` (zajednička shema iz upute + pomoćni stupci `sirovi_tekst`, `pravila_json`, `llm_json`, `prvi_run_id`), `izvori_log` (po pokretanju i izvoru), `llm_log` (tokeni i cijena), `pokretanja`.

## Pravila skupljanja

- Poštuje se `robots.txt` (npr. `fondovieu.gov.hr/download` i `hamagbicro.hr/arhiva/` se ne dohvaćaju).
- Najviše 1 zahtjev u sekundi po domeni, User-Agent `DatathonPOC/0.1 (studentski projekt)`.
- Bez prijave, bez zaobilaženja zaštite, bez slanja e-mailova.
- Polje koje nije u izvoru ostaje `null`.
