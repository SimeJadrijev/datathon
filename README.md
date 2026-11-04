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

Pri prvom startu baza se puni iz seeda (`resources/seed`). Svježe natječaje s izvora dohvaća ingest: udruge.gov.hr, FZOEU, NPOO, eKohezija (EU fondovi 2021.-2027.), HZZ, APPRRR, MZOM, 18 ministarstava (vijesti na Vladinom sustavu), 32 grada i županije, agencije (HRZZ, AMPEU), sveučilišta i studentski centri te Narodne novine. Registar udruga (`?source=ASSOCIATION_REGISTRY`, 132 MB pri prvom dohvatu) puni tablicu udruga za profil iz OIB-a. Pokreće se admin rutom, uz `ADMIN_KEY` postavljen pri pokretanju API-ja:

```bash
curl -X POST -H "X-Admin-Key: $ADMIN_KEY" "http://localhost:8080/api/admin/ingest?source=FZOEU"   # bez source = svi izvori
```

Ruta odmah vraća `202` s `runId`, a ingest radi u pozadini (prvi put nekoliko minuta, poslije iz cachea u `data/cache/`). Rezultat po izvoru (dohvaćeno, novo, promijenjeno, greške) je u tablici `source_run_log`. Ingest se može pokrenuti i pri startu sa `INGEST_ON_STARTUP=true`, ili svaki dan sa `INGEST_SCHEDULE_ENABLED=true` (vrijeme u `INGEST_SCHEDULE_CRON`, zadano 5:30).

Ključevi (`SUDREG_CLIENT_ID`, `SUDREG_CLIENT_SECRET`, `ANTHROPIC_API_KEY`) drže se u `apps/api/.env`, koji nije u gitu. Spring ga ne čita sam, pa ga učitaj prije pokretanja:

```bash
set -a; . ./.env; set +a
ADMIN_KEY=... ./mvnw spring-boot:run
```

Javne rute za dijeljenje podataka:
- `GET /api/funding-calls/feed.atom?targetGroup=STUDENT&county=...`: Atom feed novih natječaja za iste filtre kao lista (gumb "Prati nove (RSS)" na webu).
- `GET /api/funding-calls/export?status=OPEN`: natječaji u predloženom standardu "Otvoreni podaci o natječajima", shema na `/api/standard/funding-call.schema.json`.

#### AI rezultati i osvježavanje seeda

AI koraci trebaju `ANTHROPIC_API_KEY` (bez ključa se preskaču). Rezultati se spremaju u bazu, a izvozom u seed dobiva ih svatko i bez ključa. Batchevi rade u pozadini, jedan po jedan (dok jedan radi, drugi vraća `409`):

```bash
curl -X POST -H "X-Admin-Key: $ADMIN_KEY" "http://localhost:8080/api/admin/ai/extract?limit=300"              # AI-2, otvoreni prvi
curl -X POST -H "X-Admin-Key: $ADMIN_KEY" "http://localhost:8080/api/admin/ai/generate-questions?limit=300"   # AI-4, samo otvoreni
curl -X POST -H "X-Admin-Key: $ADMIN_KEY" "http://localhost:8080/api/admin/ai/summarize-regulations?limit=60" # AI-3
```

Kad završe, izvezi bazu u seed i commitaj oba filea:

```bash
curl -H "X-Admin-Key: $ADMIN_KEY" http://localhost:8080/api/admin/seed/funding-calls > src/main/resources/seed/funding-calls.json
curl -H "X-Admin-Key: $ADMIN_KEY" http://localhost:8080/api/admin/seed/regulations   > src/main/resources/seed/regulations.json
```

Seed se učitava samo u praznu bazu. Postojeću lokalnu bazu za novi seed obriši s `docker compose down -v` pa `docker compose up -d`.

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
