# Datathon

Prototip za Datathon Portala otvorenih podataka 2026. Monorepo s web aplikacijom i API-jem.

## Struktura

| Mapa | Opis |
|---|---|
| `apps/web` | Frontend: React 19, TypeScript, Vite, wouter, TanStack Query, axios |
| `apps/api` | Backend: Spring Boot 4, Java 25, JPA, PostgreSQL |
| `context/`, `work/`, `team/` | Dokumentacija natjecanja i tima (nije dio aplikacije) |

## Preduvjeti

- Node 22.13+ (ili 24+) i Yarn 1. Na starijem Nodeu `yarn install` javlja grešku "engine node is incompatible".
- JDK 25
- Docker (za PostgreSQL)

## Pokretanje

### 1. Baza

```bash
cd apps/api
docker compose up -d
```

Postgres radi na `localhost:5433` (baza, korisnik i lozinka: `datathon`). Postavke se mogu nadjačati varijablama `DB_URL`, `DB_USER`, `DB_PASSWORD`.

### 2. API

```bash
cd apps/api
./mvnw spring-boot:run      # Windows: mvnw.cmd spring-boot:run
```

Sluša na `http://localhost:8080`.

`mvnw` je Maven Wrapper: sam skine verziju Mavena propisanu u projektu, pa nije potrebna lokalna instalacija. Ako imaš vlastiti Maven (3.9+), možeš koristiti `mvn` umjesto `./mvnw` (npr. `mvn clean install`).

Pri prvom startu baza se puni iz seeda (`resources/seed`). Svježe natječaje s izvora (udruge.gov.hr, FZOEU) dohvaća ingest. Pokreće se admin rutom, uz `ADMIN_KEY` postavljen pri pokretanju API-ja:

```bash
curl -X POST -H "X-Admin-Key: $ADMIN_KEY" "http://localhost:8080/api/admin/ingest?source=FZOEU"   # bez source = svi izvori
```

Ruta odmah vraća `202` s `runId`, a ingest radi u pozadini (prvi put nekoliko minuta, poslije iz cachea u `data/cache/`). Rezultat po izvoru je u tablici `source_run_log`. Ingest se može pokrenuti i pri startu sa `INGEST_ON_STARTUP=true`.

### 3. Web

```bash
cd apps/web
yarn install
yarn dev
```

Otvara se na `http://localhost:5173`.

## Korisne naredbe (apps/web)

| Naredba | Opis |
|---|---|
| `yarn dev` | dev server |
| `yarn build` | provjera tipova + produkcijski build |
| `yarn lint` | ESLint |

## Napomene

- Web poziva API na putanji `/api` (axios `baseURL`). U `yarn dev` Vite proxy prosljeđuje `/api` na `http://localhost:8080`, pa API mora biti pokrenut. Ako nije, ekrani s podacima pokazuju poruku o grešci.
- Web stranice bez podataka (layout, navigacija) rade i bez baze i API-ja: dovoljan je samo korak 3.
