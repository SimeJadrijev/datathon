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
