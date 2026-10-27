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
