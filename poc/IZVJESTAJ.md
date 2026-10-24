# Izvještaj POC-a: agregator natječaja, poticaja i stipendija

_Generirano 2026-10-24 skriptom `run_all.py` / `report.py`. Brojke su iz `data/natjecaji.sqlite`._

**Ukupno u bazi: 1485 natječaja/poziva iz 11 izvora; otvorenih: 191; najavljenih: 5.** LLM izvlačenje: 2 zapisa.

## Zaključak

**Preporuka: ideja je izvediva do 9. 11., ali samo kao suženi demo** (6 izvora, fokus na „profil ili OIB -> relevantni otvoreni natječaji -> obavijest"). Kao „svi natječaji u Hrvatskoj s rezultatima" nije izvediva ni do 9. 11. ni s 4 osobe nakon toga.

### Što je pokazano (brojke iz ovog pokretanja)
- **~1.500 natječaja iz 11 modula, oko 190 otvorenih**, za ~3-4 minute (1 zahtjev/s po domeni). Nijedan izvor nije tražio login, CAPTCHA ni JavaScript. Svih 8 skupina izvora iz upute radi.
- **4 izvora imaju javni API** (WordPress REST: APPRRR, HZZ, Osijek, Rijeka-vijesti). Ostalo je statični HTML koji se lako parsira.
- **Ured za udruge (udruge.gov.hr) je najvrjedniji izvor**: središnji katalog natječaja za udruge od SVIH davatelja (ministarstva, gradovi, općine, zajednice), ~97 otvorenih i ~3.250 zatvorenih, a detalj ima strukturirana polja: županija, rok, ukupni/min/max iznos. To je de facto već „otvoreni podatak o natječajima", samo bez API-ja.
- **Rok i iznos pravilima (bez LLM-a)**: na otvorenim natječajima rok ima ~56 %, iznos ~50 %. Kod strukturiranih izvora (udruge, fondovieu, MZOM) rok je ~100 % kad se dohvati detalj; kod FZOEU, HZZ i HAMAG rok i iznos su u PDF-u ili su „do utroška sredstava", pa tu treba LLM.
- **Podudaranje radi** za sva 3 profila: građanin s e-autom dobije FZOEU poziv za vozila (EnU-2/26) na prvom mjestu, student državne i bilateralne stipendije s rokom, IT d.o.o. (OIB -> NKD 62.01 -> digitalizacija/inovacije) vaučere za digitalizaciju, HZZ potpore i riječki Startup inkubator. Simulacija obavijesti (`notify_dry_run.py`) za zadnjih 14 dana šalje sva 3 korisnika smislene nove natječaje.
- **LLM trošak je zanemariv**: procjena ~0,0006 USD po natječaju s Haiku 5.5, < 1 USD za cijeli skup. Novac nije prepreka; točnost jest nepoznanica.

### Najveći rizici (kritički)
1. **Točnost LLM-a nije izmjerena.** U ovom okruženju nema `ANTHROPIC_API_KEY`, pa je LLM kod napisan, ali nije pokrenut. Prije odluke: pokrenuti `python run_all.py --llm-limit 5`, pa `--llm-limit 300` i ispuniti `eval/uzorak.csv` (15 natječaja, ~1 h rada jedne osobe).
2. **„Tko je dobio novac" je slaba točka.** Skup „Podaci o financiranju udruga" ima ~90.000 isplata s OIB-om primatelja, ali **podaci završavaju 2016.** (metapodaci kažu ažurirano 2026., sadržaj ne). Rijeka: samo 2018. i link vraća 404. Karlovac: DOCX. Zagreb inovatori: 2014.-2015. Rezultate ne obećavati u demu, ili ih prikazati kao „ovo nedostaje, evo standarda" (vidi dolje).
3. **Kriterij „upotreba otvorenih podataka" (prednost živim API-jima):** naši glavni podaci NISU s data.gov.hr, nego s web stranica tijela. Na data.gov.hr praktički nema skupova o natječajima („natječaj" 5, „stipendije" 1 skup). Žiri to može vidjeti kao scraping, a ne korištenje otvorenih podataka. Ublažavanje: povezati s data.gov.hr gdje stvarno ima smisla (Registar udruga po OIB-u, Popis javnopravnih tijela kao šifrarnik davatelja, NKD 2025, Popis JLS) i **Sudski registar API** (treba registracija za OAuth ključ: zatražiti ODMAH, ne znamo koliko traje).
4. **Pravila za ciljne skupine i područja su preširoka** (npr. poziv za udžbenike označen kao „student", HZZ mjere kao „poljoprivrednik"). Za demo to mora riješiti LLM ili ručno označen skup od ~50 demo natječaja.
5. **Duplikati među izvorima** (22 ista naziva: MZOM/AMPEU, Osijek/udruge) i **šum u izvorima** (vijesti o dodijeljenim potporama, radionice, oglasi). Filtar po naslovu hvata većinu, ali ne sve.
6. **Održavanje**: 11 parsera = 11 mjesta koja pucaju kad tijelo promijeni stranicu. Zagreb (5/5) i Rijeka (4/5) su preskupi za vrijednost koju daju.
7. **Konkurencija postojećim kanalima**: fondovieu.gov.hr već ima „Pretplati se na obavijesti", a udruge.gov.hr već je središnji katalog za udruge. Vrijednost mora biti u spajanju izvora + profilu/OIB-u + objašnjenju „zašto ti ovo odgovara", ne u samom popisu.

### Izvori za demo
| Uključiti | Zašto |
|---|---|
| Ured za udruge | strukturiran, sve razine vlasti, pokriva i gradove/općine za udruge |
| FZOEU | najjači „wow" primjer za video (poticaj za e-auto, solari) |
| HZZ mjere | REST API, mjere za poslodavce i nezaposlene |
| APPRRR | REST API + RSS, poljoprivrednici |
| MZOM | stipendije s rokom u popisu |
| fondovieu (NPOO) | rok 100 %, nadležno tijelo u detalju |

Izostaviti: Zagreb i Rijeka (skupo, nisko pokriće), AMPEU (većinom pozivi institucijama, stipendije se preklapaju s MZOM). HAMAG i Osijek opcionalno. Sljedeći kandidat: **eufondovi.gov.hr** (EU fondovi 2021.-2027., HAMAG linka tamo).

### Prijedlog standarda „Otvoreni podaci o natječajima"
Najjači argument prema žiriju (MPUDT je organizator i vlasnik data.gov.hr): ne „scrapali smo 11 stranica", nego „evo koliko je to teško, i evo standarda koji bi to riješio jednim API-jem". Prijedlog:

- **Razina skupa (DCAT-AP / DCAT-AP HR)**: svako tijelo objavljuje jedan `dcat:Dataset` „Natječaji, javni pozivi i potpore – <tijelo>" s `dct:publisher` (tijelo), `dcat:theme` = `ECON` (+ `ENVI`, `EDUC`, `AGRI` po potrebi, EU Data Theme rječnik), `dct:accrualPeriodicity` = `daily`/`continuous`, `dct:spatial` (NUTS/LAU), licenca otvorena dozvola RH, i `dcat:Distribution` u JSON-u (po mogućnosti `dcat:accessService` = API). Portal skuplja sve skupove preko CKAN-a, kao sad.
- **Razina zapisa (JSON)**: shema iz ovog POC-a, s izmjenama koje omogućuju spajanje:
  - `id` = trajni URI kod tijela; `davatelj` kao objekt s **OIB-om** i oznakom iz Popisa javnopravnih tijela;
  - `status` iz zatvorenog rječnika (`najavljen|otvoren|zatvoren|ponisten`), datumi ISO 8601 (`rok_prijave` može biti `null` uz `rok_napomena: "do utroška sredstava"`);
  - iznosi kao `{"iznos": 20000000, "valuta": "EUR"}`;
  - `ciljne_skupine` iz kontroliranog rječnika, `nkd_sifre` po **NKD 2025**, `podrucje` kao **NUTS 2021 / LAU** kodovi umjesto naziva županija;
  - `prilozi` s `mediaType`; `izvor_financiranja` (proračun, EU fond i program);
  - **`rezultati`**: popis dodjela `{oib_primatelja, naziv, iznos, datum_odluke}` ili poveznica na zaseban skup rezultata s istim `id`-om natječaja. To je ključ koji danas nedostaje: skup o financiranju udruga nema ID natječaja, pa se rezultati ne mogu spojiti s pozivom.

```json
{
  "id": "https://www.fzoeu.hr/natjecaji/EnU-2-26",
  "naziv": "Javni poziv za sufinanciranje energetski učinkovitih vozila za fizičke osobe (EnU-2/26)",
  "davatelj": {"naziv": "Fond za zaštitu okoliša i energetsku učinkovitost", "oib": "<OIB tijela>", "jpt_oznaka": "<oznaka>"},
  "vrsta": "javni poziv",
  "status": "otvoren",
  "datum_objave": "2026-10-28",
  "rok_prijave": null,
  "rok_napomena": null,
  "iznos_ukupno": {"iznos": 20000000, "valuta": "EUR"},
  "iznos_po_korisniku": {"min": null, "max": null},
  "ciljne_skupine": ["gradjanin"],
  "podrucja": ["energetika", "promet", "okolis"],
  "nkd_sifre": [],
  "podrucje_provedbe": ["HR0"],
  "izvor_financiranja": "nacionalni",
  "uvjeti_sazetak": "Fizičke osobe s prebivalištem u RH, za nova vozila kategorija L1-L7 i M1 na električni pogon.",
  "prilozi": [{"naziv": "Javni poziv EnU-2/26", "url": "https://www.fzoeu.hr/docs/275/Javni poziv EnU-2_26.pdf", "mediaType": "application/pdf"}],
  "rezultati": {"url": null, "dodjele": []},
  "azurirano": "2026-10-24T12:00:00+02:00"
}
```
_(OIB i oznaka tijela su rezervirana mjesta; rok i iznos po korisniku ostavljeni su `null` jer ih POC nije provjerio u PDF-u poziva.)_

### Sljedeći koraci prije zaključavanja ideje (~27. 10.)
1. Postaviti `ANTHROPIC_API_KEY`, pokrenuti LLM na 5 pa na ~300 natječaja i ispuniti `eval/uzorak.csv` (cilj: rok i iznos ≥ 85 % točni).
2. Zatražiti OAuth ključ za Sudski registar.
3. Pustiti `jury-simulator` na suženu verziju ideje (demo s 6 izvora + standard) prije odluke.

## 1. Izvori

Svih 8 skupina izvora iz upute je pokušano (11 modula). „Dohvaćeno" = stavke s popisa nakon filtriranja onoga što nije financiranje (oglasi za posao, nabava, prodaja nekretnina). Detalji i PDF-ovi su namjerno ograničeni na 40 detalja i 15 natječaja s PDF-om po izvoru (1 zahtjev/s po domeni), pa „s rokom/iznosom" za velike izvore podcjenjuje ono što je moguće.

| Skupina | Izvor | Metoda | Dohvaćeno | Otvorenih | S rokom | S iznosom | S PDF-om (preuzeto) | Vrijeme | HTTP (cache) | Težina 1-5 | Problemi |
|---|---|---|---:|---:|---:|---:|---:|---:|---:|:---:|---|
| 1. APPRRR | apprrr | RSS + WordPress REST API | 91 | 5 | 56 | 31 | 18 (14) | 123 s | 0 (56) | 2 | RSS ima samo 10 objava; arhiva preko WP REST API-ja. Dokumenti su na zasebnoj stranici intervencije, a ne u objavi. Objave miješaju natječaje, izmjene i obavijesti o isplatama. |
| 2. FZOEU | fzoeu | statični HTML (jedna stranica, accordion) | 28 | 8 | 7 | 13 | 28 (15) | 17 s | 0 (36) | 2 | Zadani URL /hr/natjecaji/7 preusmjerava; pravi popis je jedna accordion stranica. Status je u CSS klasi. Rok je samo u tekstu/PDF-u, često 'do utroška sredstava'. |
| 3. EU fondovi (NPOO) | fondovieu | server-renderirani HTML (Nuxt SSR), paginacija | 142 | 8 | 142 | 40 | 2 (2) | 35 s | 0 (60) | 3 | Nuxt SSR radi bez JS-a, ali filtar 'u najavi' nije pronađen; robots.txt zabranjuje /download pa prilozi nisu dostupni. Pokriva samo NPOO; pozivi 2021.-2027. su na eufondovi.gov.hr. |
| 4. HAMAG-BICRO | hamag | statični HTML (WordPress), paginacija | 110 | 23 | 24 | 21 | 15 (15) | 99 s | 0 (69) | 3 | WP bez REST API-ja; grid s kategorijama u CSS klasama. Dio stavki vodi na eufondovi.gov.hr. Puno vijesti (webinari, najave) među natječajima. |
| 5. HZZ mjere | hzz | WordPress REST API (custom post type `measures`) | 97 | 16 | 16 | 40 | 97 (15) | 144 s | 0 (32) | 1 | Čist WP REST API s taksonomijama (godina, ciljna skupina). Mjere su kontinuirane: nema roka ni ukupnog iznosa u tekstu. |
| 6. Ured za udruge | udruge | statični HTML, paginacija + filtar statusa | 246 | 96 | 40 | 40 | 1 (1) | 44 s | 0 (107) | 1 | Najbolji izvor: središnji katalog s filtrima i strukturiranim detaljem (županija, rok, ukupni/min/max iznos). Tekst natječaja je samo poveznica na stranicu davatelja. Na izvoru ukupno: {'otvoreni': 97, 'zatvoreni': 3250}. |
| 7. Stipendije | mzom | statični HTML, paginacija | 91 | 10 | 77 | 14 | 7 (7) | 62 s | 0 (62) | 2 | Isti CMS kao udruge.gov.hr; 'Rok:' u popisu. Popis miješa oglase za posao i obavijesti (filtrira se po naslovu). Na izvoru ukupno: 1547. |
| 7. Stipendije | ampeu | statični HTML (Craft CMS), paginacija | 237 | 5 | 36 | 7 | 8 (8) | 86 s | 0 (66) | 2 | Statični HTML s paginacijom; 'Rok je prošao' kao oznaka statusa. Većina stavki su Erasmus+/CEEPUS pozivi institucijama i obavijesti o radionicama. |
| 8. Gradovi | osijek | WordPress REST API (custom post type `natjecaji`) | 292 | 1 | 4 | 1 | 92 (10) | 29 s | 0 (21) | 2 | WP REST API s tipom 'natjecaji', ali tip miješa savjetovanja, oglase, prodaju zemljišta (~57 % se filtrira). Sadržaj je gotovo uvijek samo PDF/DOCX u ACF polju. Na izvoru ukupno: 1042. |
| 8. Gradovi | rijeka | statični HTML + WordPress REST API (vijesti) | 111 | 12 | 67 | 63 | 9 (4) | 26 s | 0 (14) | 4 | Natječaji za financiranje nisu na jednom mjestu: stranica 'Ostali natječaji' ima 1-2 stavke, ostalo su vijesti. Pretraga vijesti donosi i izvještaje o već dodijeljenom novcu. |
| 8. Gradovi | zagreb | statični HTML, više ručno uređenih tematskih stranica | 40 | 7 | 28 | 18 | 15 (15) | 133 s | 0 (69) | 5 | Nema jedinstvenog popisa; tematske stranice uređene ručno, vlastiti CMS bez semantičkih klasa, stare stavke (2024.) se ne uklanjaju. Za svaku temu treba poseban URL. |

Isti natječaj u više izvora (isti normalizirani naziv): **22** (npr. MZOM i AMPEU objavljuju iste stipendije, Osijek i Ured za udruge iste pozive za udruge). Konačni proizvod treba deduplikaciju; `match.py` ih spaja po nazivu.

## 2. Potpunost polja

Cijeli skup (1485 zapisa). „Prije" = samo pravila/regex i strukturirani podaci izvora; „poslije" = nakon LLM-a (2 zapisa obrađeno LLM-om). Na otvorenim i detaljno dohvaćenim natječajima potpunost je bitno veća, vidi drugu tablicu.

| Polje | Prije LLM (svi) | Poslije LLM (svi) | Prije LLM (s detaljima, n=856) | Poslije LLM (s detaljima) |
|---|---:|---:|---:|---:|
| id | 100 % | 100 % | 100 % | 100 % |
| izvor | 100 % | 100 % | 100 % | 100 % |
| url | 100 % | 100 % | 100 % | 100 % |
| naziv | 100 % | 100 % | 100 % | 100 % |
| davatelj | 100 % | 100 % | 100 % | 100 % |
| vrsta | 98 % | 98 % | 99 % | 99 % |
| status | 69 % | 69 % | 55 % | 55 % |
| datum_objave | 93 % | 93 % | 99 % | 99 % |
| rok_prijave | 33 % | 33 % | 41 % | 41 % |
| iznos_ukupno_eur | 12 % | 12 % | 21 % | 21 % |
| iznos_po_korisniku_min_eur | 6 % | 6 % | 10 % | 10 % |
| iznos_po_korisniku_max_eur | 16 % | 16 % | 28 % | 28 % |
| ciljne_skupine | 75 % | 75 % | 79 % | 79 % |
| podrucja | 83 % | 83 % | 88 % | 88 % |
| nkd_sifre | 3 % | 3 % | 5 % | 5 % |
| zupanije | 86 % | 86 % | 100 % | 100 % |
| uvjeti_sazetak | 0 % | 0 % | 0 % | 0 % |
| potrebni_dokumenti | 0 % | 0 % | 0 % | 0 % |
| prilozi | 26 % | 26 % | 45 % | 45 % |
| sirovi_tekst_hash | 100 % | 100 % | 100 % | 100 % |
| dohvaceno | 100 % | 100 % | 100 % | 100 % |

## 3. Točnost LLM-a na uzorku

Tim još nije ručno ispunio `eval/uzorak.csv`, pa točnost nije izmjerena. Postupak:

1. `python eval/make_sample.py` (15 nasumičnih natječaja s tekstom; uz ključ pokreće i `claude-sonnet-5-5` na istom uzorku za usporedbu).
2. Za svaki redak otvoriti `url` i upisati točne vrijednosti u stupce `tocno_*` (rok, iznos max, iznos ukupno, ciljne skupine, područja). Prazno = polje ne postoji u izvoru.
3. `python eval/usporedi.py` ispisuje točnost po polju za pravila, Haiku i Sonnet; `python run_all.py --samo-izvjestaj` je upisuje ovdje.

**Privremena mjera (nije točnost): slaganje s `claude-sonnet-5-5` kao referencom** na istom uzorku (n=15, nasumično iz natječaja koje je Haiku obradio). Rok i iznosi: isti datum / iznos unutar 1 %; skupine i područja: prosječna Jaccard sličnost skupova.

| Polje | Pravila vs Sonnet | Haiku vs Sonnet |
|---|---:|---:|
| rok_prijave | 87 % | 100 % |
| iznos_po_korisniku_max_eur | 67 % | 87 % |
| iznos_ukupno_eur | 93 % | 100 % |
| ciljne_skupine | 73 % | 65 % |
| podrucja | 61 % | 81 % |

Visoko slaganje dvaju modela ne dokazuje točnost (mogu griješiti isto), ali pokazuje da jeftiniji Haiku daje praktički iste vrijednosti kao 20x skuplji Sonnet. Konačnu točnost daje tek ručna provjera.

## 4. Trošak LLM-a

| Model | Poziva | Ulazni tokeni | Izlazni tokeni | Trošak USD | USD po natječaju |
|---|---:|---:|---:|---:|---:|
| claude-haiku-5-5 | 307 | 1920416 | 139397 | 0.2617 | 0.00085 |
| claude-sonnet-5-5 | 30 | 196888 | 12642 | 0.5202 | 0.01734 |

- **claude-haiku-5-5**: ~3447 ulaznih + ~450 izlaznih tokena po natječaju ≈ **0.00057 USD/natječaj**; za 626 natječaja s tekstom ≈ 0.36 USD, za cijeli skup (1485) ≈ 0.85 USD.
- **claude-sonnet-5-5**: ~3447 ulaznih + ~450 izlaznih tokena po natječaju ≈ **0.01139 USD/natječaj**; za 626 natječaja s tekstom ≈ 7.13 USD, za cijeli skup (1485) ≈ 16.92 USD.

Cijene: Haiku 5.5 (0.1, 0.5) USD, Sonnet 5.5 (2.0, 10.0) USD po milijun tokena (ulaz, izlaz). Budžet POC-a je ograničen u kodu na 1.5 USD (`POC_LLM_BUDGET_USD`).

## 5. Sudski registar (OIB -> NKD + sjedište)

- OpenAPI dokumentacija: HTTP 200, 39 endpointa, sigurnost: ['Oauth2', 'Authorization'].
- **Registracija je potrebna, ali je besplatna i samostalna**: obrazac na sudreg-data.gov.hr (naziv, e-mail, tip „Javni korisnik"), nakon potvrde e-maila odmah se dobiju Client ID i Client Secret. Pozivi idu s OAuth2 *client credentials* tokenom (`https://sudreg-data.gov.hr/api/oauth/token`). Bez ključa `detalji_subjekta` vraća HTTP 401.
- Ključ u ovom okruženju: DA, korišten pravi API. Izmišljeni OIB-ovi iz mocka uvijek idu na mock, pa testni profili rade i bez ključa.
- **Dohvat po OIB-u radi** (provjereno na stvarnim tvrtkama): `detalji_subjekta?tip_identifikatora=oib&identifikator=<OIB>` vraća `tvrtka`, `sjediste` (županija, općina, naselje, adresa), `pravni_oblik.vrsta_pravnog_oblika.kratica` i djelatnost.
- **Djelatnost**: `pretezita_djelatnost` je za javne korisnike prazna, ali `glavna_djelatnost` sadrži **NKD 2007 šifru bez točke** (1072 -> 10.72 Proizvodnja dvopeka i keksa, 4120 -> 41.20, 150 -> 01.50). To NIJE ID iz šifrarnika `/nacionalna_klasifikacija_djelatnosti` (tamo je 1072 „Vađenje željeznih ruda", NKD 1994). `predmeti_poslovanja` imaju šifre iz starijih NKD verzija (1994, 2002), pa ih POC ne koristi. NKD odjeljak se preslikava u područja interesa (`opendata/nkd.py`).
- Ograničenja: obrti nisu u Sudskom registru (vode se u Obrtnom registru, koji POC nije provjeravao), pa prijava OIB-om radi za trgovačka društva, zadruge i ustanove. Podaci se osvježavaju jednom dnevno radnim danom.

Primjer stvarnog odgovora (skraćeno) i profila koji iz njega nastaje:

```json
{
  "potpuni_oib": "92803032010",
  "glavna_djelatnost": 1072,
  "tvrtka": {
    "ime": "KOESTLIN dioničko društvo,tvornica keksa i vafla",
    "naznaka_imena": "KOESTLIN"
  },
  "sjediste": {
    "sifra_zupanije": 7,
    "naziv_zupanije": "Bjelovarsko-bilogorska županija",
    "sifra_opcine": 248,
    "naziv_opcine": "Bjelovar",
    "sifra_naselja": 3239,
    "naziv_naselja": "Bjelovar",
    "sifra_ulice": 32390164,
    "ulica": "Slavonska cesta",
    "kucni_broj": 2,
    "kucni_podbroj": "a"
  },
  "pravni_oblik": {
    "vrsta_pravnog_oblika": {
      "sifra": 4,
      "naziv": "dioničko društvo",
      "kratica": "d.d."
    }
  }
}
```

```json
{
  "naziv": "KOESTLIN dioničko društvo,tvornica keksa i vafla",
  "tip": "doo",
  "nkd": [
    "10.72"
  ],
  "interesi": [
    "poduzetnistvo",
    "inovacije",
    "zaposljavanje"
  ],
  "lokacija": {
    "grad": "Bjelovar",
    "zupanija": "Bjelovarsko-bilogorska županija"
  },
  "izvor_profila": "sudreg_api"
}
```

## 6. Rezultati natječaja (tko je dobio novac) na data.gov.hr

| Skup | Uloga | Izdavatelj | Ažurirano | Formati | Čitljivo | Redaka | OIB primatelja | Napomena |
|---|---|---|---|---|---|---:|---|---|
| Podaci o financiranju udruga | rezultati | Ured za udruge | 2026-04-15 | XLS 9 | da | 89646 | da (12654 različitih) | godine 2004-2016 |
| Subvencije poduzetnicima Grada Rijeke - Lista korisnika | rezultati | Grad Rijeka | 2026-03-13 | XLS 1, CSV 1 | ne |  |  | HTTPStatusError: http://data.rijeka.hr/dataset/0db8f795-f8eb-4580-b164-8bd2d38bb055/resource/f2f791f6-a9bb- |
| Potpore poduzetnicima - Grad Karlovac | rezultati | Grad Karlovac | 2026-04-28 | ? 2, DOCX 4 | ne |  |  | nema tabličnog resursa (DOCX/PDF/link na web) |
| Potpore inovatorima Grada Zagreba | rezultati | Grad Zagreb | 2026-04-15 | XLSX 2, CSV 2 | da | 30 |  |  |
| Registar proračunskih i izvanproračunskih korisnika | javna_tijela | Ministarstvo financija | 2026-07-27 | HTML 1, XLSX 4 |  |  |  |  |
| Popis brojčanih oznaka javnopravnih tijela u elektroničkom o | javna_tijela | Ministarstvo pravosuđa, uprave i digitalne transformacije | 2026-07-24 | JSON 3 |  |  |  |  |
| Popis županija, gradova i općina | javna_tijela | Ministarstvo pravosuđa, uprave i digitalne transformacije | 2026-04-15 | HTML 1, XLS 2 |  |  |  |  |
| Nacionalna klasifikacija djelatnosti 2025. | klasifikacija | Državni zavod za statistiku | 2026-04-15 | JSON 1, XML 1 |  |  |  |  |
| Registar udruga Republike Hrvatske | udruge | Ministarstvo pravosuđa, uprave i digitalne transformacije | 2026-10-24 | HTML 1, JSON 3, XML 3, CSV 3 |  |  |  |  |

Pretraga kataloga (broj skupova): „potpore" 27, „subvencije" 9, „stipendije" 1, „natječaj" 5, „dodijeljene potpore" 3, „lista korisnika" 3.

Primjer spoja: u skupu „Podaci o financiranju udruga" (gradovi) Grad Rijeka ima 1713 isplata udrugama; iznos po godinama: {'2013': 24652823, '2014': 23252579, '2015': 26226817, '2016': 24623751}. Spaja se s natječajima po davatelju i godini (ne po ID-u natječaja, jer ga skup nema), a s Registrom udruga po OIB-u.

## 7. Podudaranje: 3 testna profila

### Student iz Rijeke, interes okoliš

Profil: tip `student` + gradjanin, interesi ['okolis', 'obrazovanje', 'znanost'], lokacija {'grad': 'Rijeka', 'zupanija': 'Primorsko-goranska'}.

| # | Natječaj | Izvor | Status | Rok | Zašto |
|---|---|---|---|---|---|
| 1 | [Natječaj za stipendije Nacionalnoga stipendijskog programa Slovačke Republike u ](https://www.ampeu.hr/natjecaji/natjecaj-za-stipendije-nacionalnoga-stipendijskog-programa-slovacke-republike-u-akademskoj-godini-2026-2027-ljetni-semestar) | ampeu | otvoren | 2026-10-31 | ciljna skupina: student; područje: obrazovanje, znanost; ključne riječi u nazivu: stipendij; otvoren; rok uskoro (2026-10-31) |
| 2 | [Javni natječaj za dodjelu državnih stipendija za studente pripadnike romske naci](https://mzom.gov.hr/istaknute-teme/natjecaji-196/8068) | mzom | otvoren | 2026-10-31 | ciljna skupina: student; područje: obrazovanje, znanost; ključne riječi u nazivu: stipendij; otvoren; rok uskoro (2026-10-31) |
| 3 | [Natječaj za dodjelu 12.150 državnih stipendija za akademsku godinu 2026./2027. s](https://mzom.gov.hr/istaknute-teme/natjecaji-196/natjecaj-za-dodjelu-12-150-drzavnih-stipendija-za-akademsku-godinu-2026-2027-studentima-u-redovitom-statusu-koji-studiraju-na-visokim-ucilistima-u-republici-hrvatskoj/8096) | mzom | otvoren | 2026-11-03 | ciljna skupina: student; područje: obrazovanje, znanost; ključne riječi u nazivu: stipendij; otvoren; rok uskoro (2026-11-03) |
| 4 | [Javni poziv za dodjelu državnih stipendija u STEM područjima znanosti za akadems](https://mzom.gov.hr/javni-poziv-za-dodjelu-drzavnih-stipendija-u-stem-podrucjima-znanosti-za-akademsku-godinu-2026-2027-studentima-u-redovitom-statusu-koji-su-upisani-na-visoka-ucilista-u-republici-hrvatskoj/8114) | mzom | otvoren | 2026-11-11 | ciljna skupina: student; područje: obrazovanje, znanost; ključne riječi u nazivu: stipendij; otvoren; rok uskoro (2026-11-11) |
| 5 | [Natječaj za stipendije Švicarske Konfederacije u akademskoj godini 2027./2028.](https://mzom.gov.hr/istaknute-teme/natjecaji-196/natjecaj-za-stipendije-svicarske-konfederacije-u-akademskoj-godini-2027-2028/7990) | mzom | otvoren | 2026-11-30 | ciljna skupina: student; područje: obrazovanje, znanost; ključne riječi u nazivu: stipendij; otvoren |
| 6 | [Natječaj za stipendije Slobodne Države Bavarske za diplomske i poslijediplomske ](https://www.ampeu.hr/natjecaji/natjecaj-za-stipendije-slobodne-drzave-bavarske-za-diplomske-i-poslijediplomske-studije-u-akademskoj-godini-2027-2028) | ampeu | otvoren | 2026-12-01 | ciljna skupina: student; područje: obrazovanje, znanost; ključne riječi u nazivu: stipendij; otvoren |
| 7 | [Natječaj Zaklade „Dr. Viktor Ružić“ za dodjelu stipendija učenicima i studentima](https://www.rijeka.hr/natjecaj-zaklade-dr-viktor-ruzic-za-dodjelu-stipendija-ucenicima-i-studentima/) | rijeka | otvoren | 2027-06-30 | ciljna skupina: student; lokalno (Primorsko-goranska); područje: obrazovanje; ključne riječi u nazivu: stipendij; otvoren |
| 8 | [Objavljeni prijedlozi rang-lista za dodjelu stipendija Grada Rijeke u školskoj/a](https://www.rijeka.hr/objavljeni-prijedlozi-rang-lista-za-dodjelu-stipendija-grada-rijeke-u-skolskoj-akademskoj-godini-2025-2026/) | rijeka | ? | ? | status nepoznat; ciljna skupina: student; lokalno (Primorsko-goranska); područje: obrazovanje, znanost; ključne riječi u nazivu: stipendij |

### Građanin koji želi kupiti električni automobil

Profil: tip `gradjanin`, interesi ['promet', 'energetika'], lokacija {'grad': 'Zagreb', 'zupanija': 'Grad Zagreb'}.

| # | Natječaj | Izvor | Status | Rok | Zašto |
|---|---|---|---|---|---|
| 1 | [Javni poziv za sufinanciranje energetski učinkovitih vozila za fizičke osobe (En](https://www.fzoeu.hr/hr/nacionalni-javni-pozivi-i-natjecaji/1367#275) | fzoeu | otvoren | ? | ciljna skupina: gradjanin; područje: promet, energetika; ključne riječi u nazivu: vozil; otvoren |
| 2 | [Biram Hrvatsku – mobilnost radne snage](https://mjere.hzz.hr/mjere/biram-hrvatsku-mobilnost-radne-snage-2026/) | hzz | otvoren | ? | ciljna skupina: gradjanin; područje: promet; otvoren |

### Mala IT tvrtka (d.o.o.), profil iz Sudskog registra po OIB-u

Profil: tip `doo` + obrt, interesi ['digitalizacija', 'inovacije', 'zaposljavanje'], lokacija {'grad': 'Rijeka', 'zupanija': 'Primorsko-goranska županija'}, NKD ['62.01'] (iz Sudskog registra, sudreg_mock).

| # | Natječaj | Izvor | Status | Rok | Zašto |
|---|---|---|---|---|---|
| 1 | [Objavljen Javni poziv za upis pružatelja usluga u Katalog pružatelja usluga u ok](https://hamagbicro.hr/objavljen-javni-poziv-za-upis-pruzatelja-usluga-u-katalog-pruzatelja-usluga-u-okviru-poziva-vauceri-za-digitalizaciju/) | hamag | otvoren | ? | ciljna skupina: doo; područje: digitalizacija; ključne riječi u nazivu: digital, vaučer; otvoren |
| 2 | [Potpore za zapošljavanje – zeleno/digitalno](https://mjere.hzz.hr/mjere/potpore-za-zaposljavanje-zeleno-digitalno-2026/) | hzz | otvoren | ? | ciljna skupina: doo; područje: digitalizacija, zaposljavanje; ključne riječi u nazivu: digital; otvoren |
| 3 | [Potpore za pripravništvo i mlade – zeleno/digitalno](https://mjere.hzz.hr/mjere/potpore-za-pripravnistvo-i-mlade-zeleno-digitalno-2026/) | hzz | otvoren | ? | ciljna skupina: doo; područje: digitalizacija, zaposljavanje; ključne riječi u nazivu: digital; otvoren |
| 4 | [Predstavljanje Javnog poziva za 18. generaciju korisnika Startup inkubatora Rije](https://www.rijeka.hr/predstavljanje-javnog-poziva-za-18-generaciju-korisnika-startup-inkubatora-rijeka/) | rijeka | otvoren | 2026-11-16 | ciljna skupina: doo; lokalno (Primorsko-goranska); područje: inovacije; ključne riječi u nazivu: startup; otvoren; rok uskoro (2026-11-16) |
| 5 | [Dodjela potpora za komercijalizaciju inovacija riječkim poduzetnicima](https://www.rijeka.hr/dodjela-potpora-za-komercijalizaciju-inovacija-rijeckim-poduzetnicima/) | rijeka | ? | ? | status nepoznat; ciljna skupina: doo; lokalno (Primorsko-goranska); područje: inovacije; ključne riječi u nazivu: inovac |
| 6 | [Grad Rijeka i Zaklada FIPRO: Potpisani ugovori o dodjeli bespovratnih potpora ri](https://www.rijeka.hr/grad-rijeka-i-zaklada-fipro-potpisani-ugovori-o-dodjeli-bespovratnih-potpora-rijeckim-inovatorima/) | rijeka | ? | ? | status nepoznat; ciljna skupina: doo; lokalno (Primorsko-goranska); područje: inovacije, zaposljavanje |
| 7 | [Potpora za očuvanje radnih mjesta kod poslodavaca pogođenih posljedicama požara ](https://mjere.hzz.hr/mjere/potpora-za-ocuvanje-radnih-mjesta-kod-poslodavaca-pogodenih-posljedicama-pozara/) | hzz | otvoren | 2026-10-31 | ciljna skupina: doo; područje: zaposljavanje; otvoren; rok uskoro (2026-10-31) |
| 8 | [Poziv na dostavu projektnih prijedloga u sklopu FOREST poziva 2026. godine](https://mzom.gov.hr/istaknute-teme/natjecaji-196/poziv-na-dostavu-projektnih-prijedloga-u-sklopu-forest-poziva-2026-godine/8027) | mzom | otvoren | 2026-12-02 | ciljna skupina: doo; područje: inovacije; otvoren |

