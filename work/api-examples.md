# API: primjeri odgovora

Primjeri za web pakete (TypeScript tipovi). Svaki korak dodaje svoj odjeljak. Primjeri iz stvarnog poziva (`curl`) su označeni; ostali su oblik koji provjeravaju testovi.

## API-1: zajednički oblici (oblik provjeravaju testovi, nije dohvaćeno curlom)

Greška (svaki status 4xx i 5xx), `message` je na hrvatskom i prikazuje se korisniku:
```json
{ "statusCode": 404, "message": "Natječaj ne postoji.", "error": "Not Found" }
```
```json
{ "statusCode": 400, "message": "q: smije imati najviše 200 znakova", "error": "Bad Request" }
```
```json
{ "statusCode": 502, "message": "Vanjski izvor (Sudreg) trenutno nije dostupan. Pokušajte kasnije.", "error": "Bad Gateway" }
```

Stranica (sve liste), `page` kreće od 0, `size` je najviše 100:
```json
{ "content": [], "totalElements": 250, "page": 0, "size": 20 }
```

## API-2: natječaji (stvarni odgovori, `curl` na seed od 2026-10-26)

`GET /api/funding-calls` filteri (svi neobavezni): `q` (najviše 200 znakova, bez razlike u velikim slovima i dijakriticima), `status` (`OPEN` je default, `ANNOUNCED`, `CLOSED`, `UNKNOWN` = natječaji bez statusa), `targetGroup`, `area`, `county`, `source`, `type`, `deadlineBefore` (ISO datum, uključivo), `page` (od 0), `size` (default 20, najviše 100), `sort` (`deadline` = najbliži rok prvi, prazni rokovi na kraju, default; `publishedAt` = najnoviji prvi). Skriveni natječaji i duplikati se nikad ne vraćaju.

```bash
curl "http://localhost:8080/api/funding-calls?status=OPEN&targetGroup=COMPANY&size=2"
```
```json
{
  "content": [
    {
      "id": 99,
      "title": "Financiranje provedbe već započetih investicijskih projekata koji se odnose na manje dijelove sustava javne vodoopskrbe i javne odvodnje otpadnih voda",
      "provider": "Ministarstvo zaštite okoliša i zelene tranzicije",
      "source": { "code": "EU_FUNDS_NPOO", "name": "EU fondovi (NPOO)" },
      "type": "PUBLIC_CALL",
      "status": "OPEN",
      "deadline": "2023-06-13",
      "deadlineNote": null,
      "totalAmount": 106178246.73,
      "maxAmount": 106178246.73,
      "targetGroups": ["CITIZEN", "COMPANY"],
      "areas": ["ENVIRONMENT", "ENTREPRENEURSHIP"],
      "counties": ["ALL"],
      "extractedByLlm": false,
      "url": "https://fondovieu.gov.hr/pozivi/10"
    },
    {
      "id": 360,
      "title": "Mjera za zadržavanje radnih mjesta u prerađivačkoj industriji",
      "provider": "Hrvatski zavod za zapošljavanje",
      "source": { "code": "HZZ", "name": "Hrvatski zavod za zapošljavanje (mjere)" },
      "type": "SUBSIDY",
      "status": "OPEN",
      "deadline": "2026-01-31",
      "deadlineNote": null,
      "totalAmount": null,
      "maxAmount": 300000.0,
      "targetGroups": ["CITIZEN", "CRAFT_BUSINESS", "COMPANY"],
      "areas": ["EMPLOYMENT", "ENTREPRENEURSHIP"],
      "counties": ["ALL"],
      "extractedByLlm": false,
      "url": "https://mjere.hzz.hr/mjere/mjera-za-zadrzavanje-radnih-mjesta-u-preradivackoj-industriji-2026/"
    }
  ],
  "totalElements": 20,
  "page": 0,
  "size": 2
}
```

`GET /api/funding-calls/{id}`:
```json
{
  "id": 360,
  "title": "Mjera za zadržavanje radnih mjesta u prerađivačkoj industriji",
  "provider": "Hrvatski zavod za zapošljavanje",
  "source": { "code": "HZZ", "name": "Hrvatski zavod za zapošljavanje (mjere)", "url": "https://mjere.hzz.hr" },
  "type": "SUBSIDY",
  "status": "OPEN",
  "publishedAt": "2026-01-01",
  "deadline": "2026-01-31",
  "deadlineNote": null,
  "totalAmount": null,
  "minAmount": 1050.0,
  "maxAmount": 300000.0,
  "targetGroups": ["CITIZEN", "CRAFT_BUSINESS", "COMPANY"],
  "areas": ["EMPLOYMENT", "ENTREPRENEURSHIP"],
  "counties": ["ALL"],
  "conditionsSummary": null,
  "requiredDocuments": [],
  "attachments": [
    { "name": "Potpore za zapošljavanje Preuzmite u PDF formatu PREUZMI", "url": "https://mjere.hzz.hr/app/uploads/2026/01/1.-Potpore-za-zaposljavanje.pdf" }
  ],
  "extractedByLlm": false,
  "aiFields": [],
  "fieldsReviewed": false,
  "fetchedAt": "2026-10-26T18:06:24Z",
  "url": "https://mjere.hzz.hr/mjere/mjera-za-zadrzavanje-radnih-mjesta-u-preradivackoj-industriji-2026/"
}
```

Nepoznat id (`/api/funding-calls/999999`): `{"statusCode":404,"message":"Natječaj ne postoji.","error":"Not Found"}`.
Neispravan filter (`?status=XYZ`): `{"statusCode":400,"message":"status: neispravna vrijednost","error":"Bad Request"}`.

Napomene za web: nepoznati `status` filter (`UNKNOWN`) nije enum `FundingCallStatus`, pa tip filtera u TypeScriptu treba imati 4 vrijednosti, a tip `status` u odgovoru 3 (`status` može biti `null`). `counties` može sadržati `ALL` (cijela Hrvatska).

---

## API-3: `GET /api/funding-calls/facets`

Prima iste filtere kao lista (`page`, `size`, `sort` se ignoriraju). Svaki facet ignorira vlastiti filter, ostale primjenjuje. Zadani `status=OPEN` primjenjuje se na sve facete osim `status`. Sortirano po `count` silazno. Prazan status je `value: "UNKNOWN"`, a sve oznake cijele države u `county` spojene su u `ALL`.

Bez filtera (`/api/funding-calls/facets`, skraćeno):

```json
{
  "status": [{ "value": "CLOSED", "count": 503 }, { "value": "OPEN", "count": 141 }, { "value": "UNKNOWN", "count": 49 }],
  "targetGroup": [{ "value": "ASSOCIATION", "count": 98 }, { "value": "LOCAL_GOVERNMENT", "count": 42 }, { "value": "STUDENT", "count": 10 }],
  "area": [{ "value": "CIVIL_SOCIETY", "count": 37 }, { "value": "CULTURE", "count": 35 }, { "value": "EDUCATION", "count": 24 }],
  "county": [{ "value": "ALL", "count": 55 }, { "value": "Primorsko-goranska", "count": 8 }, { "value": "Zagrebačka", "count": 1 }],
  "source": [{ "value": "ASSOCIATIONS_OFFICE", "count": 94 }, { "value": "HZZ", "count": 16 }, { "value": "APPRRR", "count": 5 }],
  "type": [{ "value": "PUBLIC_CALL", "count": 77 }, { "value": "SUBSIDY", "count": 33 }, { "value": "UNKNOWN", "count": 3 }]
}
```

Sa `?targetGroup=STUDENT&status=OPEN`: `targetGroup` i dalje pokazuje sve skupine, a `area`, `county`, `source` i `type` broje samo studentske natječaje.

```json
{
  "status": [{ "value": "CLOSED", "count": 36 }, { "value": "OPEN", "count": 10 }, { "value": "UNKNOWN", "count": 4 }],
  "targetGroup": [{ "value": "ASSOCIATION", "count": 98 }, { "value": "LOCAL_GOVERNMENT", "count": 42 }, { "value": "STUDENT", "count": 10 }],
  "area": [{ "value": "EDUCATION", "count": 10 }, { "value": "SCIENCE", "count": 7 }, { "value": "YOUTH", "count": 2 }],
  "county": [{ "value": "ALL", "count": 10 }],
  "source": [{ "value": "MZOM", "count": 8 }, { "value": "EU_FUNDS_NPOO", "count": 2 }],
  "type": [{ "value": "SCHOLARSHIP", "count": 6 }, { "value": "PUBLIC_CALL", "count": 4 }]
}
```

Napomene za web:
- Tip: `FacetCount = { value: string; count: number }`, `FundingCallFacets = Record<'status'|'targetGroup'|'area'|'county'|'source'|'type', FacetCount[]>`.
- `value` je uvijek string. Za `status` i `type` može biti `"UNKNOWN"` (za `status` je to i valjana vrijednost filtera, za `type` nije jer filter `type` nema `UNKNOWN`).
- Zbroj facetova po kolekcijama (`targetGroup`, `area`, `county`) može biti veći ili manji od ukupnog broja jer natječaj ima više vrijednosti ili nijednu. Za ukupan broj koristi zbroj `status` ili `source`.
- `county` vrijednosti su kako su u bazi i mogu se slati kao `?county=`.

---

## API-4: Propisi

### `GET /api/regulations?type=LAW&publishedFrom=2026-01-01&size=2`

Filteri: `q`, `type`, `area`, `publishedFrom`, `publishedTo` (oba uključiva), `page`, `size`. Sort je uvijek `publishedAt` silazno (prazni datumi na kraju).

```json
{
  "content": [
    {
      "id": 2598,
      "title": "Zakon o izmjenama i dopunama Zakona o zahtjevima za pristupačnost proizvoda i usluga",
      "type": "LAW",
      "gazetteNumber": "114/2026",
      "publishedAt": "2026-10-07",
      "areas": [],
      "hasSummary": false
    },
    {
      "id": 2597,
      "title": "Zakon o izmjenama i dopunama Zakona o obrtu",
      "type": "LAW",
      "gazetteNumber": "114/2026",
      "publishedAt": "2026-10-07",
      "areas": [],
      "hasSummary": false
    }
  ],
  "totalElements": 67,
  "page": 0,
  "size": 2
}
```

### `GET /api/regulations/2598`

```json
{
  "id": 2598,
  "title": "Zakon o izmjenama i dopunama Zakona o zahtjevima za pristupačnost proizvoda i usluga",
  "type": "LAW",
  "gazetteNumber": "114/2026",
  "documentNumber": 1371,
  "publishedAt": "2026-10-07",
  "enactedAt": "2026-09-25",
  "areas": [],
  "summary": null,
  "url": "https://narodne-novine.nn.hr/clanci/sluzbeni/2026_10_114_1371.html",
  "eliUrl": "https://narodne-novine.nn.hr/eli/sluzbeni/2026/114/1371",
  "pdfUrl": "https://narodne-novine.nn.hr/eli/sluzbeni/2026/114/1371/pdf",
  "eliMetadata": true,
  "fetchedAt": "2026-10-26T18:14:24Z"
}
```

### `GET /api/regulations/facets`

Isti filteri, isto pravilo (svaki facet ignorira vlastiti filter). Nema zadanog filtera, pa je ovo cijela baza.

```json
{
  "type": [{ "value": "DECISION", "count": 1920 }, { "value": "OTHER", "count": 963 }, { "value": "ORDINANCE", "count": 610 }, { "value": "DECREE", "count": 198 }, { "value": "LAW", "count": 169 }],
  "area": [{ "value": "HEALTH", "count": 178 }, { "value": "ENTREPRENEURSHIP", "count": 152 }, { "value": "AGRICULTURE", "count": 137 }]
}
```

Greške:
- Nepoznat id (`/api/regulations/999999`): `{"statusCode":404,"message":"Propis ne postoji.","error":"Not Found"}`
- `?publishedFrom=2026-05-01&publishedTo=2026-04-01`: `{"statusCode":400,"message":"publishedFrom: ne smije biti nakon publishedTo","error":"Bad Request"}`

Napomene za web:
- `summary` je `null` dok AI-3 ne generira sažetak, a `hasSummary` na listi to već kaže.
- Većina propisa nema `areas` (prazna lista), pa facet `area` ne pokriva sve propise.
- `RegulationType` ima 5 vrijednosti: `LAW`, `DECREE`, `ORDINANCE`, `DECISION`, `OTHER`.

---

## API-5: Izvori i meta

### `GET /api/meta`

```json
{"snapshotDate":"2026-10-26","lastIngestAt":null,"counts":{"openFundingCalls":141,"regulations":3860}}
```

`lastIngestAt` je `null` dok ne postoji završen `IngestRun` (INGEST paket). `snapshotDate` je datum seeda. `openFundingCalls` broji vidljive pozive sa statusom `OPEN`.

### `GET /api/sources` (3 od 8 zapisa, redoslijed kao u `SourceCode`)

```json
[
  {
    "code": "HZZ",
    "name": "Hrvatski zavod za zapošljavanje (mjere)",
    "description": "Mjere aktivne politike zapošljavanja, kontinuirane tijekom godine, preko javnog WordPress REST API-ja.",
    "url": "https://mjere.hzz.hr",
    "method": "API",
    "license": "nije provjereno",
    "recordCount": 97,
    "lastSuccessfulFetch": "2026-10-26T00:00:00Z"
  },
  {
    "code": "NARODNE_NOVINE",
    "name": "Narodne novine",
    "description": "Službeni dio Narodnih novina: zakoni i propisi s ELI identifikatorima i metapodacima.",
    "url": "https://narodne-novine.nn.hr",
    "method": "HTML",
    "license": "nije provjereno",
    "recordCount": 3860,
    "lastSuccessfulFetch": "2026-10-26T00:00:00Z"
  },
  {
    "code": "SUDREG",
    "name": "Sudski registar",
    "description": "Javni API Sudskog registra (OAuth2). Iz OIB-a tvrtke dohvaća sjedište, pravni oblik i djelatnost za izradu profila.",
    "url": "https://sudreg-data.gov.hr",
    "method": "API",
    "license": "nije provjereno",
    "recordCount": null,
    "lastSuccessfulFetch": "2026-10-26T00:00:00Z"
  }
]
```

Napomene za web:
- `method`: `API` | `HTML` | `PDF`. `license` je za sada svugdje `"nije provjereno"`.
- `recordCount` je `null` za `SUDREG` (nije izvor zapisa). Za `NARODNE_NOVINE` to je broj propisa, za ostale broj natječaja (uključujući skrivene).
- `lastSuccessfulFetch`: zadnji uspješan `SourceRunLog` (`errors = 0`), a bez njega datum seeda (ponoć UTC). Može biti `null` samo ako seed nema datum.

---

## API-6: `POST /api/profiles/match`

Body `ProfileRequest`: `type` je obavezan, a barem jedno od `areas` / `keywords` / `activityCodes` mora biti neprazno. `keywords` najviše 10 stavki od po 50 znakova, `activityCodes` oblika `62` ili `62.01`. Odgovor je lista od najviše 20 `MatchResult`, sortirana po `score` silazno, pa po roku uzlazno (prazni rokovi na kraju). `statusUnknown = true` znači da natječaj nema status (UI treba upozoriti), bez kazne u bodovima.

### Student iz Rijeke (okoliš, obrazovanje)

```json
{
  "type": "STUDENT",
  "additionalTypes": [
    "CITIZEN"
  ],
  "areas": [
    "ENVIRONMENT",
    "EDUCATION",
    "SCIENCE"
  ],
  "county": "Primorsko-goranska",
  "keywords": [
    "stipendij",
    "okoliš",
    "klimat"
  ]
}
```

Odgovor (prvi rezultat, ukupno ovisi o profilu):

```json
// ukupno rezultata: 20
{
  "fundingCall": {
    "id": 449,
    "title": "Javni natječaj za dodjelu državnih stipendija za studente pripadnike romske nacionalne manjine upisane na sveučilišne i stručne studije na visokim učilištima u Republici Hrvatskoj, za akademsku godinu 2026./2027.",
    "provider": "Ministarstvo znanosti, obrazovanja i mladih",
    "source": {
      "code": "MZOM",
      "name": "Ministarstvo znanosti, obrazovanja i mladih"
    },
    "type": "SCHOLARSHIP",
    "status": "OPEN",
    "deadline": "2026-10-31",
    "deadlineNote": null,
    "totalAmount": null,
    "maxAmount": null,
    "targetGroups": [
      "CRAFT_BUSINESS",
      "STUDENT"
    ],
    "areas": [
      "EDUCATION",
      "SCIENCE",
      "YOUTH"
    ],
    "counties": [
      "ALL"
    ],
    "extractedByLlm": false,
    "url": "https://mzom.gov.hr/istaknute-teme/natjecaji-196/8068"
  },
  "score": 15.5,
  "reasons": [
    "ciljna skupina: studenti",
    "područje: obrazovanje, znanost",
    "ključne riječi u naslovu: stipendij",
    "otvoren",
    "rok za 3 dana"
  ],
  "statusUnknown": false
}
```

### Građanin koji želi električni automobil

```json
{
  "type": "CITIZEN",
  "areas": [
    "TRANSPORT",
    "ENERGY"
  ],
  "county": "Grad Zagreb",
  "keywords": [
    "vozil",
    "električn",
    "e-mobilnost"
  ]
}
```

Odgovor (prvi rezultat, ukupno ovisi o profilu):

```json
// ukupno rezultata: 2
{
  "fundingCall": {
    "id": 267,
    "title": "Javni poziv za sufinanciranje energetski učinkovitih vozila za fizičke osobe (EnU-2/26)",
    "provider": "Fond za zaštitu okoliša i energetsku učinkovitost",
    "source": {
      "code": "FZOEU",
      "name": "Fond za zaštitu okoliša i energetsku učinkovitost"
    },
    "type": "SUBSIDY",
    "status": "OPEN",
    "deadline": "2028-03-31",
    "deadlineNote": null,
    "totalAmount": 20000000.0,
    "maxAmount": 2500.0,
    "targetGroups": [
      "CITIZEN"
    ],
    "areas": [
      "ENERGY",
      "TRANSPORT"
    ],
    "counties": [
      "ALL"
    ],
    "extractedByLlm": false,
    "url": "https://www.fzoeu.hr/hr/nacionalni-javni-pozivi-i-natjecaji/1367#275"
  },
  "score": 15.0,
  "reasons": [
    "ciljna skupina: građani",
    "područje: promet, energetika",
    "ključne riječi u naslovu: vozil",
    "otvoren"
  ],
  "statusUnknown": false
}
```

### Mala IT tvrtka (d.o.o., NKD 62.01)

```json
{
  "type": "COMPANY",
  "additionalTypes": [
    "CRAFT_BUSINESS"
  ],
  "areas": [
    "DIGITALIZATION",
    "INNOVATION",
    "EMPLOYMENT"
  ],
  "county": "Zagreb",
  "keywords": [
    "digital",
    "inovac",
    "vaučer",
    "startup"
  ],
  "activityCodes": [
    "62.01"
  ]
}
```

Odgovor (prvi rezultat, ukupno ovisi o profilu):

```json
// ukupno rezultata: 10
{
  "fundingCall": {
    "id": 349,
    "title": "Potpore za zapošljavanje – zeleno/digitalno",
    "provider": "Hrvatski zavod za zapošljavanje",
    "source": {
      "code": "HZZ",
      "name": "Hrvatski zavod za zapošljavanje (mjere)"
    },
    "type": "SUBSIDY",
    "status": "OPEN",
    "deadline": null,
    "deadlineNote": null,
    "totalAmount": null,
    "maxAmount": 2420.0,
    "targetGroups": [
      "CITIZEN",
      "CRAFT_BUSINESS",
      "COMPANY",
      "FARMER"
    ],
    "areas": [
      "ENVIRONMENT",
      "EDUCATION",
      "EMPLOYMENT",
      "ENTREPRENEURSHIP",
      "DIGITALIZATION"
    ],
    "counties": [
      "ALL"
    ],
    "extractedByLlm": false,
    "url": "https://mjere.hzz.hr/mjere/potpore-za-zaposljavanje-zeleno-digitalno-2026/"
  },
  "score": 15.0,
  "reasons": [
    "ciljna skupina: tvrtke",
    "područje: digitalizacija, zapošljavanje",
    "ključne riječi u naslovu: digital",
    "otvoren"
  ],
  "statusUnknown": false
}
```

Greške:
- Profil bez područja, ključnih riječi i djelatnosti: `{"statusCode":400,"message":"Odaberite barem jedno područje, ključnu riječ ili djelatnost.","error":"Bad Request"}`
- Prazno ili neispravno tijelo (i nepoznat `type`): `{"statusCode":400,"message":"Neispravno tijelo zahtjeva.","error":"Bad Request"}`
- Bez `type`: `{"statusCode":400,"message":"type: obavezno polje","error":"Bad Request"}`

Napomene za web:
- `MatchResult = { fundingCall: FundingCallSummary; score: number; reasons: string[]; statusUnknown: boolean }`.
- `reasons` su gotove hrvatske rečenice, prikazuju se doslovno. To su transparentna pravila, ne AI.
- Ime županije se normalizira (`Istarska`, `Istarska županija` i `istarska` su isto), `Grad Zagreb` i `Zagreb` također.
- Natječaji sa `ALL` županijom ili bez županija ne ovise o županiji profila.

---

## API-7: `POST /api/profiles/from-oib`

Body `{ "oib": "<11 znamenki>" }`. OIB se provjerava (duljina, samo znamenke, kontrolna znamenka ISO 7064 MOD 11,10), pa se tek onda poziva Sudreg. Dok nema pravog klijenta (INGEST-9) radi `FixtureSudregClient` sa SETUP-1 fixtureima (samo tri demo OIB-a, ostali valjani OIB-ovi daju 404). Odgovor je spreman za izravno slanje na `POST /api/profiles/match` (polje `profile`).

### 21173725829: Microblink d.o.o. (IT, Zagreb)

```json
{
  "companyName": "Microblink društvo s ograničenom odgovornošću za informacijske tehnologije",
  "seat": {
    "municipality": "Zagreb",
    "county": "Grad Zagreb"
  },
  "activityCode": "62.01",
  "profile": {
    "type": "COMPANY",
    "additionalTypes": [],
    "areas": [
      "DIGITALIZATION",
      "INNOVATION",
      "EMPLOYMENT"
    ],
    "county": "Grad Zagreb",
    "keywords": [],
    "activityCodes": [
      "62.01"
    ]
  },
  "source": "sudreg"
}
```

### 36201212847: Valamar Riviera d.d. (turizam, Poreč)

```json
{
  "companyName": "Valamar Riviera dioničko društvo za turizam",
  "seat": {
    "municipality": "Poreč - Parenzo",
    "county": "Istarska županija"
  },
  "activityCode": "55.10",
  "profile": {
    "type": "COMPANY",
    "additionalTypes": [],
    "areas": [
      "TOURISM",
      "EMPLOYMENT"
    ],
    "county": "Istarska županija",
    "keywords": [],
    "activityCodes": [
      "55.10"
    ]
  },
  "source": "sudreg"
}
```

### 94989605030: Kraš d.d. (prehrana, Zagreb)

```json
{
  "companyName": "KRAŠ prehrambena industrija d.d.",
  "seat": {
    "municipality": "Zagreb",
    "county": "Grad Zagreb"
  },
  "activityCode": "10.82",
  "profile": {
    "type": "COMPANY",
    "additionalTypes": [],
    "areas": [
      "ENTREPRENEURSHIP",
      "INNOVATION",
      "EMPLOYMENT"
    ],
    "county": "Grad Zagreb",
    "keywords": [],
    "activityCodes": [
      "10.82"
    ]
  },
  "source": "sudreg"
}
```

Greške:
- Kriva kontrolna znamenka, krivi broj znamenki ili slova: `400 {"statusCode":400,"message":"oib: neispravan OIB","error":"Bad Request"}`
- Bez `oib`: `400 {"statusCode":400,"message":"oib: obavezno polje","error":"Bad Request"}`
- Ispravan OIB koji nije u Sudregu: `404 {"statusCode":404,"message":"Tvrtka s ovim OIB-om nije pronađena.","error":"Not Found"}`
- Sudreg nedostupan: `502 {"statusCode":502,"message":"Vanjski izvor (Sudreg) trenutno nije dostupan. Pokušajte kasnije.","error":"Bad Gateway"}`

Napomene za web:
- `OibProfileResponse = { companyName: string; seat: { municipality: string; county: string }; activityCode: string | null; profile: ProfileRequest; source: "sudreg" }`.
- `profile.areas` je prijedlog iz NKD-a, korisnik ga smije promijeniti prije pretrage. `profile.keywords` je prazan, a `activityCodes` ima najviše jednu šifru.
- `seat.county` je naziv iz Sudrega ("Grad Zagreb", "Istarska županija"), matching ga normalizira.
- OIB se nigdje ne logira (provjereno na živom API-ju: uspjeh, 400, 404 i neispravan JSON).
