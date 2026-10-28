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
