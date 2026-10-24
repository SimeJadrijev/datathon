# POC: Agregator natječaja, poticaja i stipendija (Hrvatska)

> Ovaj dokument je uputa za Claudea (Claude Code). Zalijepi ga cijelog kao prvu poruku u novu sesiju u praznom folderu.

## 1. Kontekst

Studentski tim sudjeluje na **Datathonu Portala otvorenih podataka 2026** (organizator: Ministarstvo pravosuđa, uprave i digitalne transformacije, MPUDT). Rješenje se prijavljuje u **izazov C: Gospodarstvo i učinkovito upravljanje**. Rok za predaju je **9. 11. 2026.**

**Ideja konačnog proizvoda:** jedno mjesto na kojem se vide **svi natječaji, javni pozivi, poticaji i stipendije** u Hrvatskoj: otvoreni, nadolazeći i zatvoreni, s rezultatima (tko je dobio i koliko). Korisnik napravi profil (student, građanin, obrt, d.o.o., udruga, poljoprivrednik + područje interesa + lokacija). Tvrtke se mogu prijaviti OIB-om, pa se djelatnost (NKD) i sjedište povuku iz Sudskog registra. Kad izađe novi natječaj koji korisniku odgovara (npr. FZOEU poticaj za električna vozila), korisnik dobije **e-mail ili push obavijest**. AI strukturira natječaje iz HTML-a i PDF-ova i odgovara na pitanje „Ispunjavam li uvjete?".

**Glavni problem:** natječaji **ne postoje kao strojno čitljivi otvoreni podaci**. Razbacani su po desetcima web stranica, često u PDF-u.

## 2. Cilj OVOG POC-a

**Ne gradimo aplikaciju.** Gradimo brzi test izvedivosti koji odgovara na pitanja:

1. **Koliko natječaja** možemo pouzdano dohvatiti iz svakog izvora?
2. **Koja polja** možemo izvući (naziv, davatelj, rok, iznos, ciljna skupina, područje, link, PDF dokumentacija) i **koliko su potpuna**?
3. **Koliko je teško** održavati svaki izvor (RSS, statični HTML, JS-renderiran, PDF, zaštita od botova)?
4. **Koliko dobro LLM** izvlači strukturirane podatke iz teksta ili PDF-a natječaja (točnost na uzorku)?
5. Radi li **podudaranje profila i natječaja** preko OIB-a (Sudski registar → NKD → relevantni natječaji)?

Na kraju treba nastati **izvještaj s brojkama**, na temelju kojeg tim odlučuje ide li s ovom idejom.

## 3. Izvori koje treba testirati

Za svaki izvor najprije **istraži strukturu stranice** (popis natječaja, paginacija, detalji, prilozi), provjeri `robots.txt`, a tek onda piši parser.

| # | Izvor | URL (polazna točka) | Što pokriva | Napomena |
|---|---|---|---|---|
| 1 | APPRRR | https://www.apprrr.hr/feed/ | poljoprivreda, ruralni razvoj | **RSS feed postoji** (provjereno). Iz feeda filtriraj natječaje. |
| 2 | Fond za zaštitu okoliša i energetsku učinkovitost (FZOEU) | https://www.fzoeu.hr/hr/natjecaji/7 | e-vozila, solarni paneli, energetska obnova | HTML |
| 3 | EU fondovi | https://fondovieu.gov.hr/pozivi | otvoreni i najavljeni EU pozivi | HTML, možda renderiran JS-om |
| 4 | HAMAG-BICRO | https://hamagbicro.hr/natjecaji/ | potpore i zajmovi poduzetnicima | HTML |
| 5 | HZZ mjere | https://mjere.hr/ | zapošljavanje, samozapošljavanje | HTML |
| 6 | Ured za udruge | https://udruge.gov.hr/ | natječaji za udruge | HTML. Pronađi stranicu s kalendarom ili popisom natječaja. |
| 7 | Stipendije | Ministarstvo znanosti, obrazovanja i mladih (mzom.gov.hr) + AMPEU (ampeu.hr) | državne stipendije, Erasmus | Pronađi stranice s natječajima. |
| 8 | 2 do 3 grada | npr. Grad Rijeka (rijeka.hr), Grad Zagreb (zagreb.hr), Grad Osijek (osijek.hr) | lokalne potpore i stipendije | HTML ili PDF |

Opcionalno, ako ostane vremena: Narodne novine (oglasni dio), EOJN (eojn.hr, javna nabava, vjerojatno izvan opsega).

### Otvoreni podaci za povezivanje (data.gov.hr, CKAN API)

API: `https://data.gov.hr/ckan/api/3/action/package_search?q=...` i `package_show?id=...`

| Svrha | Dataset (CKAN `id`) |
|---|---|
| Rezultati: tko je dobio | `podaci-o-financiranju-udruga`, „Subvencije poduzetnicima Grada Rijeke – Lista korisnika" (pretraži), „Potpore poduzetnicima – Grad Karlovac" |
| Popis svih javnih tijela | `registar-proracunskih-i-izvanproracunskih-korisnika`, `popis-javnopravnih-tijela`, `popis-zupanija-gradova-i-opcina` |
| Klasifikacija djelatnosti | „Nacionalna klasifikacija djelatnosti 2025." (DZS, JSON) |
| Udruge po djelatnosti | `registar-udruga` (JSON/CSV „Djelatnosti") |
| Tvrtke po OIB-u | **Sudski registar API**: https://sudreg-data.gov.hr/ (OpenAPI dokumentacija: https://sudreg-data.gov.hr/api/javni/dokumentacija/open_api). **Provjeri treba li registracija za ključ (client credentials).** Ako treba, napiši to u izvještaj i napravi mock s 2 do 3 primjera. |

## 4. Tehnički zahtjevi

- **Jezik:** Python 3.11+
- **Biblioteke:**
  - `httpx` za HTTP
  - `selectolax` ili `beautifulsoup4` za HTML
  - `feedparser` za RSS
  - `pdfplumber` ili `pymupdf` za PDF
  - **Playwright** samo ako izvor stvarno zahtijeva JS
- **Pohrana:** SQLite (jedna tablica `natjecaji` + tablica `izvori_log`)
- **LLM izvlačenje:** Claude API (`anthropic` SDK)
  - model `claude-haiku-5-5` za masovno izvlačenje
  - model `claude-sonnet-5-5` za usporedbu točnosti na uzorku
  - ključ iz varijable okruženja `ANTHROPIC_API_KEY`
  - **Ako ključa nema, LLM korak se preskače**, a ostatak mora raditi
- **Pristojno skupljanje:**
  - poštuj `robots.txt`
  - najviše 1 zahtjev u sekundi po domeni
  - User-Agent `DatathonPOC/0.1 (studentski projekt)`
  - lokalni cache HTML-a i PDF-a (bez ponovnog preuzimanja)
- **Bez prijave i zaobilaženja zaštite:** ako izvor traži login ili CAPTCHA, samo to zabilježi u izvještaj.
- Svaki izvor je **zaseban modul** (`sources/fzoeu.py` …) sa zajedničkim sučeljem `fetch_list() -> list[RawItem]` i `fetch_detail(item) -> RawItem`.

### Ciljna shema natječaja (JSON / SQLite)

```json
{
  "id": "fzoeu-2026-012",
  "izvor": "fzoeu",
  "url": "https://...",
  "naziv": "Javni poziv za sufinanciranje nabave energetski učinkovitih vozila",
  "davatelj": "Fond za zaštitu okoliša i energetsku učinkovitost",
  "vrsta": "poticaj | natječaj | javni poziv | stipendija | zajam | nagrada",
  "status": "najavljen | otvoren | zatvoren",
  "datum_objave": "2026-10-01",
  "rok_prijave": "2026-11-30",
  "iznos_ukupno_eur": 5000000,
  "iznos_po_korisniku_min_eur": null,
  "iznos_po_korisniku_max_eur": 9000,
  "ciljne_skupine": ["gradjanin", "obrt", "doo", "udruga", "jls", "student", "poljoprivrednik"],
  "podrucja": ["okolis", "energetika", "promet"],
  "nkd_sifre": ["49.3"],
  "zupanije": ["sve"],
  "uvjeti_sazetak": "...",
  "potrebni_dokumenti": ["..."],
  "prilozi": [{"naziv": "Tekst poziva.pdf", "url": "..."}],
  "sirovi_tekst_hash": "...",
  "izvuceno_llm": true,
  "dohvaceno": "2026-10-24T12:00:00"
}
```

## 5. Koraci

1. **Izviđanje (najviše 30 min po izvoru):** otvori stranicu, opiši strukturu i odluči o pristupu (RSS / HTML / JS / PDF). Zapiši u `docs/izvori.md`.
2. **Skupljač popisa** za svaki izvor: dohvati sve natječaje koji se mogu dohvatiti (otvorene i, ako postoji, arhivu).
3. **Skupljač detalja:** za svaki natječaj tekst stranice + linkovi na PDF priloge (preuzmi najviše 3 PDF-a po natječaju).
4. **Izvlačenje polja:**
   - (a) **pravila i regex** za ono što je u HTML-u jasno (naslov, datum)
   - (b) **LLM** za ostalo, s JSON shemom iz točke 4 (structured output / tool use)
5. **Evaluacija točnosti:**
   - nasumično odaberi 15 natječaja
   - napravi `eval/uzorak.csv` u koji tim ručno upisuje točne vrijednosti
   - skripta `eval/usporedi.py` računa točnost po polju (rok, iznos, ciljna skupina, područje)
   - pripremi skriptu i prazan CSV s izvučenim vrijednostima da ih tim samo ispravi
6. **Test podudaranja:** skripta `match.py`
   - ulaz: profil (JSON) ili OIB
   - za OIB: Sudski registar → NKD + sjedište → profil
   - izlaz: rangirani popis relevantnih natječaja s objašnjenjem zašto
   - pokaži na 3 primjera profila:
     1. student iz Rijeke, interes okoliš
     2. građanin koji želi kupiti električni automobil
     3. mala IT tvrtka (d.o.o.)
7. **Test obavijesti (bez slanja maila):** simuliraj dva pokretanja. U drugom označi „nove" natječaje i ispiši koje bi korisnike trebalo obavijestiti (`notify_dry_run.py`). **Ne šalji stvarne e-mailove.**
8. **Izvještaj:** `IZVJESTAJ.md`, sadržaj u točki 6.

## 6. Izvještaj (`IZVJESTAJ.md`), obavezni sadržaj

Tablica po izvoru:

| Izvor | Metoda | Broj dohvaćenih natječaja | Otvorenih | S rokom | S iznosom | S PDF-om | Vrijeme izvođenja | Težina održavanja (1–5) | Problemi |
|---|---|---|---|---|---|---|---|---|---|

Ostali obavezni dijelovi:

- **Potpunost polja** (% popunjenosti) za cijeli skup, prije i poslije LLM-a
- **Točnost LLM-a** na uzorku (ako je tim ispunio `uzorak.csv`), inače upute kako je izmjeriti
- **Trošak LLM-a:** broj tokena i procjena cijene po natječaju i za cijeli skup
- **Sudski registar:** radi li dohvat po OIB-u, treba li registracija, primjer odgovora
- **Rezultati natječaja:** koliko se skupova s data.gov.hr može spojiti (tko je dobio novac)
- **Rezultati podudaranja** za 3 testna profila
- **Zaključak:**
  - je li ideja izvediva do 9. 11.
  - koje izvore uključiti u demo
  - koji su najveći rizici
  - prijedlog standardnog JSON formata „Otvoreni podaci o natječajima" koji bi tijela mogla objavljivati na data.gov.hr (na temelju sheme iz točke 4, usklađen s DCAT-AP gdje ima smisla)

## 7. Struktura projekta

```
poc-natjecaji/
├── README.md              # kako pokrenuti
├── IZVJESTAJ.md           # rezultati
├── requirements.txt
├── run_all.py             # pokreće sve izvore → SQLite → izvještaj
├── sources/               # jedan modul po izvoru
├── extract/               # pravila + LLM izvlačenje
├── opendata/              # data.gov.hr CKAN, Sudski registar
├── match.py
├── notify_dry_run.py
├── eval/
├── docs/izvori.md
└── data/                  # cache + natjecaji.sqlite (u .gitignore)
```

## 8. Kriteriji gotovosti

- `python run_all.py` radi od početka do kraja na čistom računalu (uz `pip install -r requirements.txt`).
- Svih 8 izvora je **pokušano**. Za svaki je u izvještaju broj dohvaćenih natječaja ili jasno objašnjenje zašto ne radi.
- SQLite baza sadrži natječaje u zajedničkoj shemi.
- `match.py` vraća smislene rezultate za 3 testna profila.
- `IZVJESTAJ.md` sadrži sve iz točke 6.

## 9. Što NE raditi

- Ne graditi web sučelje ni frontend (to je sljedeća faza).
- Ne slati stvarne e-mailove.
- Ne zaobilaziti login, CAPTCHA ni zaštitu od botova.
- Ne trošiti više od ~2 USD na LLM pozive u POC-u. Prvo testiraj na 5 natječaja, pa skaliraj.
- Ne izmišljati podatke. Ako polje nije u izvoru, ostaje `null`.

## 10. Način rada

- Radi izvor po izvor. Nakon svakog izvora kratko javi koliko je natječaja dohvaćeno i koji su problemi.
- Ako je neki izvor neočekivano težak (više od ~45 min), zabilježi problem i prijeđi na sljedeći.
- Na kraju pokaži sažetak iz `IZVJESTAJ.md`.
