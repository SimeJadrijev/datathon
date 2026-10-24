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
