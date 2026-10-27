# Paket 4 (API): plan implementacije

_Verzija: 2026-10-27. Odluke P1-P10 donesene (osim P4, vidi odjeljak 3). Specifikacija je u `work/mvp-task-specs.md` (API-1 do API-7). Ovdje je samo KAKO, i nalazi iz koda koji mijenjaju specifikaciju._

Radi se **korak po korak, ovim redom: API-1 → API-2 → API-3 → API-4 → API-5 → API-6 → API-7**. Svaki korak završava zelenim `mvnw.cmd -q compile` + `mvnw.cmd test`, ručnim `curl`-om na pokrenutom API-ju i stvarnim JSON-om koji se sprema za PR. Sljedeći korak ne počinje dok prethodni nije gotov.

**Git (P10):** jedan branch `sime/api`, **jedan commit (ili više) po koraku** s ID-em u poruci (`API-2: Funding call list and detail`), **jedan PR za cijeli paket** na kraju. JSON primjeri se tijekom rada skupljaju u `work/api-examples.md` (po koraku), pa se kopiraju u opis PR-a. Rizik: web paketi (WEB-5 do WEB-9) čekaju do kraja paketa. Ublažavanje: `work/api-examples.md` se pusha na branch nakon svakog koraka, pa web može graditi tipove odmah.

---

## 0. Nalazi iz koda (zašto plan odstupa od specifikacije)

| # | Nalaz | Posljedica |
|---|---|---|
| N1 | Branch `feature/pkg-5-ai` (paket 5, WIP) **već ima** `common/ErrorResponse`, `GlobalExceptionHandler`, `NotFoundException`, `ExternalSourceException`, `InvalidRequestException`, `TooManyRequestsException`. Njihov handler nema 502 za `ExternalSourceException` i vraća generičku poruku za validaciju (ne "prvo polje + razlog"). | Merge konflikt na `common/` ako oba paketa stvore iste fileove. Treba dogovor (pitanje P1). |
| N2 | `@EntityGraph` / `join fetch` nad kolekcijama + paginacija: Hibernate tada **učita sve retke u memoriju** i reže stranicu u Javi (upozorenje HHH90003004). To krši "do not load the whole table". `FundingCall` ima 7 `@ElementCollection`. | Za listu ne koristimo `@EntityGraph` nad kolekcijama, nego batch fetch (pitanje P2). |
| N3 | Sudreg fixturei su u `src/test/resources/fixtures/sudreg/`. **Runtime kod ne vidi test resurse**, pa fixture klijent za demo bez ključa ne bi radio izvan testova. | Kopija u `src/main/resources/sudreg-fixtures/` (pitanje P4). |
| N4 | `GlobalExceptionHandler` iz paketa 5 logira `e.getMessage()` za neispravno tijelo. Poruka `MethodArgumentNotValidException` sadrži **odbijenu vrijednost**, tj. OIB. | Za API-7 handler ne smije logirati odbijene vrijednosti (samo ime polja). Test to provjerava. |
| N5 | Županije u seedu su sirovi stringovi: `ALL` (449), `Primorsko-goranska`, `Područje cijele Hrvatske`, `Izvan Republike Hrvatske`... Sudreg vraća `Istarska županija`, `Grad Zagreb`. | Normalizacija županija za matching i za filter `county` (pitanje P5). |
| N6 | Seed: 693 natječaja (OPEN 141, CLOSED 503, **status null 49**), 355 bez roka, 165 bez ciljne skupine; 3.860 propisa (LAW 169). `snapshotDate` = 2026-10-26 postoji samo u seed fileovima, ne u bazi. | Default `status=OPEN` skriva 49 zapisa nepoznatog statusa (P3: filter `status=UNKNOWN`). `/api/meta` čita datum iz seed filea. |
| N7 | `SourceCode` nema `SUDREG`, a API-5 traži Sudreg kao izvor i kriterij "svaki izvor iz enuma točno jednom". | Pitanje P4. |
| N8 | NKD šifre u seedu su rijetke i neuredne (`47.99`, `75.0`, `43.6`), a `glavna_djelatnost` u Sudregu je često prazna (decisions.md, SETUP-1). | Matching po NKD-u radi po odjeljku (prve 2 znamenke). API-7 bez NKD-a vraća `activityCode: null` i područja `[ENTREPRENEURSHIP, EMPLOYMENT]` (kao POC). |

---

## 1. Zajednička pravila za sve korake

- **Struktura (package by feature):**
  ```
  common/          ErrorResponse, GlobalExceptionHandler, NotFoundException, ExternalSourceException,
                   InvalidRequestException, PageResponse, PageRequests (helper: page/size → Pageable, cap 100),
                   OpenApiConfig, CountyNames, DatabaseExtensionsCheck
  fundingcall/     FundingCallController, FundingCallService, FundingCallSpecifications, FundingCallFacetQuery
                   dto/ FundingCallFilter, FundingCallStatusFilter, FundingCallSort, FundingCallSummary,
                        FundingCallDetail, SourceRef, FacetCount, FundingCallFacets
  regulation/      RegulationController, RegulationService, RegulationSpecifications
                   dto/ RegulationFilter, RegulationSummary, RegulationDetail, RegulationFacets
  source/          SourceController, SourceCatalog (čita resources/sources.json), MetaController/MetaService
                   dto/ SourceResponse, MetaResponse
  profile/         ProfileController, MatchingService, ProfileMatchService, MatchLabels, ProfileFromOibService
                   dto/ ProfileRequest, MatchResult, OibRequest, CompanyProfileResponse
  integration/sudreg/  SudregClient (interface), FixtureSudregClient, CompanyInfo, NkdAreas
  ```
- **Kontroler** prima `@Valid` record, zove jedan servis, vraća DTO. **Servis** ima `@Transactional(readOnly = true)` i mapira entitet → DTO statičkom metodom `Dto.from(entity)`.
- **Filtri** iz query parametara su `record` + `@ModelAttribute @Valid`. Neispravan enum ili datum = binding greška = 400.
- **Paginacija:** `page` od 0 (default 0), `size` default 20; veći od 100 se **tiho reže na 100** (kriterij: `size=1000` vraća najviše 100), negativni → 400.
- **Filter po kolekcijama** (`targetGroup`, `area`, `county`) preko `member of` (subquery), ne `join`, da `count` upit ne broji duplikate i da ne treba `distinct`.
- **Vidljivost:** svaki upit nad natječajima ima `hidden = false and duplicateOf is null` u jednoj metodi `FundingCallSpecifications.visible()`, koju koriste lista, detalj, faceti, matching i meta.
- **Poruke grešaka** na hrvatskom, `error` = HTTP reason phrase (`"Not Found"`), stack trace samo u log.
- **Testovi:**
  - `@WebMvcTest(XController.class)` + `@MockitoBean` servis: happy path, 404, 400 (bez baze).
  - Logika upita (filteri, faceti, vidljivost): `@DataJpaTest` nad Docker Postgresom (`replace = NONE`, kao postojeći `FundingCallRepositoryTest`), podaci iz `src/test/resources/fixtures/seed/funding-calls.json` ili ručno složeni entiteti. Traži `docker compose up -d`.
  - `MatchingService` i NKD/OIB mapiranje: čisti unit testovi bez Springa.
- **JSON primjeri:** API se pokrene sa seedom (`SEED_ENABLED=true`), `curl` na svaki novi endpoint, odgovor (skraćen na 2-3 stavke u listi) ide u `work/api-examples.md` pod naslovom koraka, a na kraju u opis PR-a.
- **Na kraju svakog koraka:** ažurirati status u `work/tasks.md`, zapis u `log.md`, nova odluka u `work/decisions.md`.

---

## 2. Koraci

### API-1: Osnova API-ja (0,5 d)
1. `common/ErrorResponse`, `NotFoundException`, `ExternalSourceException(source, message, cause)`, `InvalidRequestException`: doslovne kopije iz `feature/pkg-5-ai` (P1).
2. `common/GlobalExceptionHandler` (proširena verzija paketa 5):
   - `NotFoundException` → 404, poruka iz iznimke, hrvatska (P6).
   - `InvalidRequestException` → 400 (za provjere koje Bean Validation ne pokriva, npr. `publishedFrom > publishedTo`), poruka iz iznimke.
   - 400 za: `MethodArgumentNotValidException` (body i `@ModelAttribute`), `HandlerMethodValidationException`, `MethodArgumentTypeMismatchException`, `MissingServletRequestParameterException`, `HttpMessageNotReadableException`. Poruka = `"<polje>: <razlog>"` prvog polja; za `typeMismatch` razlog "neispravna vrijednost". **Odbijena vrijednost se nikad ne logira ni ne vraća** (N4).
   - `NoResourceFoundException` → 404 "Tražena adresa ne postoji." (inače nepoznata ruta daje 500).
   - `ExternalSourceException` → 502 "Vanjski izvor (<izvor>) trenutno nije dostupan. Pokušajte kasnije.", log `warn` s iznimkom.
   - `Exception` → 500 "Došlo je do pogreške na poslužitelju.", log `error`.
   - Bean Validation poruke pišemo na hrvatskom u anotacijama (`@Size(max = 200, message = "smije imati najviše 200 znakova")`), da ne ovisimo o lokalizaciji Hibernate Validatora.
3. `common/PageResponse<T>(List<T> content, long totalElements, int page, int size)` + `static <E, T> PageResponse<T> from(Page<E> page, Function<E, T> mapper)`.
4. `common/PageRequests.of(Integer page, Integer size, Sort sort)`: default 0/20, cap 100.
5. `common/OpenApiConfig`: `OpenAPI` bean (naslov, opis, verzija). `application.yaml`: `springdoc.swagger-ui.path: /swagger-ui.html` (spec na `/v3/api-docs` je default).
6. Testovi: `GlobalExceptionHandlerTest` = `@WebMvcTest` nad malim test kontrolerom u `src/test` (baca 404, 400, 502, 500) + provjera `size=1000` → `size: 100` u odgovoru.
7. Ručno: Swagger UI se otvara na `http://localhost:8080/swagger-ui.html`.

### API-2: Natječaji, lista i detalj (1,0 d)
1. `FundingCallRepository extends JpaSpecificationExecutor<FundingCall>`.
2. `FundingCallFilter(q, status, targetGroup, area, county, source, type, deadlineBefore, page, size, sort)`; `q` `@Size(max=200)`; `status` je `FundingCallStatusFilter` (default `OPEN`, `UNKNOWN` = `status is null`, P3); `sort` enum `FundingCallSort { DEADLINE, PUBLISHED_AT }` (vrijednosti u URL-u `deadline` / `publishedAt`, P7).
3. `FundingCallSpecifications`: `visible()`, `matchesFilter(filter)` sastavljen od malih metoda (`titleOrProviderContains`, `hasStatus`, `hasTargetGroup`...). `deadlineBefore` uključivo (`deadline <= x`). `county`: uspoređuje se normalizirano (`CountyNames`, P5), plus pozivi s `ALL` (cijela država vrijedi za svaku županiju).
   - **`q` bez dijakritika (P8):** `unaccent(lower(title)) like unaccent(lower(%q%))` preko `cb.function("unaccent", ...)`. Ekstenzija: `docker-compose.yml` dobiva init skriptu `apps/api/docker/init/01-unaccent.sql` (`CREATE EXTENSION IF NOT EXISTS unaccent;`) montiranu u `/docker-entrypoint-initdb.d/`. **Init skripte se izvršavaju samo na praznom volumenu**, pa postojeće baze trebaju jednokratno `docker compose exec postgres psql -U datathon -c "CREATE EXTENSION IF NOT EXISTS unaccent"` (u README). Uz to `common/DatabaseExtensionsCheck` pri startu izvrši isti `CREATE EXTENSION IF NOT EXISTS` (korisnik `datathon` je vlasnik baze, pa smije), da nitko ne zapne. `%` i `_` iz `q` se escapeaju. Bez indeksa: ~700 redaka, seq scan je dovoljno brz.
4. Sort: `deadline asc nulls last, id asc` (default); `publishedAt desc nulls last, id desc`. Provjeriti podržava li Spring Data JPA 4 `Sort.Order.nullsLast()` uz Specification + Hibernate 7; ako ne, `CASE WHEN deadline IS NULL` u specifikaciji.
5. N+1: `spring.jpa.properties.hibernate.default_batch_fetch_size: 100` (P2). Jedna lista = 1 upit za stranicu + 1 count + ~1 upit po kolekciji koja se čita (targetGroups, areas, counties) = ~5 upita, neovisno o broju redaka.
6. DTO: `SourceRef(code, name, url)` iz `SourceCatalog` (minimalna verzija iz `resources/sources.json` nastaje ovdje, API-5 je proširi). `FundingCallSummary.from(entity, sourceRef)`, `FundingCallDetail.from(...)` bez `sourceText`, `eligibilityQuestions`, `rawTextHash`, `hiddenReason`.
7. Detalj: `findOne(visible().and(idEquals(id)))` → inače `NotFoundException("Natječaj ne postoji.")`. Skriveni i duplikati daju 404.
8. Testovi: `FundingCallControllerTest` (lista, detalj, 404, `status=XYZ` → 400, `q` od 201 znaka → 400); `FundingCallSpecificationsTest` (`@DataJpaTest`: status+targetGroup, skriveni/duplikati izostavljeni, sort s praznim rokovima na kraju, county s `ALL`).
9. Ručno: `show-sql` uključen lokalno, jedan zahtjev liste, broj upita u PR.

### API-3: Faceti natječaja (0,5 d)
1. `GET /api/funding-calls/facets`, isti `FundingCallFilter` (page/size/sort se ignoriraju).
2. `FundingCallFacetQuery` (Criteria API, `Tuple`): za svaki facet `select value, count(c) ... group by value` nad `visible() AND filter bez vlastitog polja`. Skalarni facetovi (`status`, `source`, `type`) grupiraju po stupcu; kolekcijski (`targetGroup`, `area`, `county`) po `join` kolekcije. 6 upita po zahtjevu, sve u bazi.
3. `FundingCallFacets(List<FacetCount> status, targetGroup, area, county, source, type)`, `FacetCount(String value, long count)`, sortirano po `count desc`. Status `null` se vraća kao `value: "UNKNOWN"` (P3). `county` facet vraća vrijednosti kako su u bazi (osim `Područje cijele Hrvatske` → `ALL`), jer su to i vrijednosti filtera.
4. Testovi (`@DataJpaTest`): bez filtera zbroj `status` = broj vidljivih poziva (s obzirom na `null`, status je jedini facet koji pokriva sve); `targetGroup=STUDENT` → `area` broji samo studentske, `targetGroup` i dalje sve skupine. `@WebMvcTest` happy path.
5. Napomena: zbroj facetova po kolekcijama ne mora biti jednak ukupnom broju (jedan poziv ima više područja ili nijedno). U testu se za kriterij "zbroj = ukupno" koristi `status` (ili `source`, koji nikad nije prazan).

### API-4: Propisi (0,7 d)
1. `RegulationRepository extends JpaSpecificationExecutor<Regulation>`.
2. `RegulationFilter(q, type, area, publishedFrom, publishedTo, page, size)`, `publishedFrom > publishedTo` → 400.
3. Sort `publishedAt desc nulls last, id desc`.
4. `RegulationSummary(id, title, type, gazetteNumber, publishedAt, areas, hasSummary)`; `RegulationDetail` sa svim poljima + `summary`, `url`, `eliUrl`, `pdfUrl`, `enactedAt`, `eliMetadata`, `documentNumber`, `fetchedAt`.
5. `GET /api/regulations/facets` → `{type: [...], area: [...]}`, isti obrazac kao API-3 (zajednički mali helper u `common/` ako se kod ponavlja, inače dvije male klase).
6. Testovi: `@WebMvcTest` lista, detalj, 404; `@DataJpaTest` `type=LAW&publishedFrom=2026-01-01` → samo zakoni iz 2026, najnoviji prvi.

### API-5: Izvori i meta (0,4 d)
1. `resources/sources.json`: za svaki izvor `code`, `name`, `description`, `url`, `method` (`API`/`HTML`/`PDF`), `license` (`"nije provjereno"` ako ne znamo). Sadržaj iz `poc/docs/izvori.md`.
2. `SourceCatalog` (učita JSON pri startu, `@PostConstruct` ili konstruktor) + validacija da svaki `SourceCode` ima zapis (pad pri startu ako nema, da se ne zaboravi novi izvor).
3. `GET /api/sources`: katalog + `recordCount` (`countBySource` za natječaje, `count()` za NN, `null` za Sudreg) + `lastSuccessfulFetch` (zadnji `SourceRunLog` s `errors = 0`, inače `snapshotDate`).
4. `GET /api/meta`: `snapshotDate` (servis čita samo polje `snapshotDate` iz `seed/funding-calls.json` streaming parserom pri startu, ne cijeli file u objekte), `lastIngestAt` (zadnji `IngestRun.finishedAt`, može biti `null`), `counts.openFundingCalls` (vidljivi + OPEN), `counts.regulations`.
5. Testovi: `@WebMvcTest` happy path; unit test da `SourceCatalog` sadrži svaki `SourceCode` točno jednom; test da je `snapshotDate` = datum iz seeda.

### API-6: Matching (1,2 d)
1. `ProfileRequest(@NotNull type, additionalTypes, areas, county, keywords, activityCodes)`. Validacija (P9): `type` obavezan; barem jedno od `areas` / `keywords` / `activityCodes` neprazno, poruka "Odaberite barem jedno područje, ključnu riječ ili djelatnost."; `keywords` max 10 × 50 znakova; `activityCodes` uzorak `\d{2}(\.\d{1,2})?`.
2. `MatchingService.match(ProfileRequest, List<FundingCall>, LocalDate today)`: čista funkcija (datum se predaje, da su testovi deterministični). Servis `ProfileMatchService` dohvati kandidate iz baze (`visible()` + status ≠ CLOSED + rok ≥ danas ili null, ~200 redaka) i preda ih.
3. Pravila (port `poc/match.py::score`):
   - isključi CLOSED i istekli rok;
   - status null + bez roka + objava starija od 365 dana (ili nepoznata) → isključi (vjerojatno zatvoren, kao POC); inače `statusUnknown = true`, **bez kazne** (popravak);
   - ciljne skupine: ako postoje i nema presjeka → isključi; +4 glavni tip, +2 dodatni;
   - županija: ako poziv nije `ALL` i normalizirana županija profila nije među njegovima → isključi; pogodak +2;
   - tema: područja +3 po pogotku (max 2), ključne riječi u naslovu +3 svaka, NKD odjeljak +4; bez ijednog → isključi;
   - OPEN +2, ANNOUNCED +1, rok unutar 30 dana +0,5.
4. Duplikati: ključ = naslov lowercase, sve osim `[a-zčćšđž0-9]` → razmak, prvih 80 znakova (`_kljuc`). Sort `score desc, deadline asc nulls last`, zadržava se prvi po ključu, max 20.
5. `reasons` na hrvatskom: "ciljna skupina: studenti", "područje: okoliš, obrazovanje", "lokalno: Primorsko-goranska", "ključne riječi u naslovu: stipendij", "djelatnost NKD 62", "otvoren", "rok za 12 dana". Hrvatski nazivi enuma u `profile/MatchLabels` (privatna mapa).
6. `common/CountyNames.normalize(String)` (nastaje u API-2, koristi se i ovdje): lowercase, makni "županija", "grad ", višak razmaka; `Područje cijele Hrvatske` = `ALL` (P5). Unit test: `Istarska županija` = `Istarska`, `Grad Zagreb` = `zagreb`.
7. `POST /api/profiles/match` → `List<MatchResult(FundingCallSummary fundingCall, double score, List<String> reasons, boolean statusUnknown)>`.
8. Testovi `MatchingServiceTest` (6+ slučajeva iz specifikacije, entiteti složeni u testu) + `@WebMvcTest` (happy path, prazan body → 400, nepoznat `type` → 400, profil bez područja/ključnih riječi/NKD-a → 400).
9. PR: stvarni JSON za tri profila iz `poc/profiles/` (prevedena na nova polja; IT d.o.o. profil = Microblink iz SETUP-1).

### API-7: Profil iz OIB-a (0,5 d)
1. `integration/sudreg/SudregClient { CompanyInfo findByOib(String oib); }` (baca `NotFoundException` ili `ExternalSourceException("Sudreg", ...)`). `CompanyInfo(name, Seat(municipality, county), legalForm, activityCode)`. **Dogovoriti s vlasnikom paketa 3 (INGEST-9)** da koristi isto sučelje i isti `CompanyInfo` (P4).
2. `FixtureSudregClient`: čita `classpath:sudreg-fixtures/<oib>.json` (kopija SETUP-1 fixturea u `src/main/resources`, N3), mapira strani JSON u privatni record pa u `CompanyInfo` (polja Sudrega ne izlaze iz paketa). Aktivan kad nema `sudreg.client-id` (`@ConditionalOnProperty` / `@ConditionalOnMissingBean`), da ga pravi klijent iz INGEST-9 zamijeni bez izmjena ovdje.
3. `OibRequest(@NotBlank @Oib String oib)`: vlastita anotacija `@Oib` (11 znamenki + ISO 7064 MOD 11,10 kontrolna znamenka).
4. `ProfileFromOibService`: `CompanyInfo` → `ProfileRequest`:
   - pravni oblik → `TargetGroup`: d.o.o., j.d.o.o., d.d., k.d., j.t.d., zadruga → `COMPANY`; udruga → `ASSOCIATION`; ustanova → `LOCAL_GOVERNMENT` (kao POC, upitno); ostalo → `COMPANY`;
   - NKD (`glavna_djelatnost` 6201 → "62.01") → `activityCodes`, područja iz `NkdAreas` (port `poc/opendata/nkd.py`, mapirano na enum `Area`, + `EMPLOYMENT`); bez NKD-a → `[ENTREPRENEURSHIP, EMPLOYMENT]`;
   - `county` = normalizirani naziv županije sjedišta.
5. Odgovor: `{companyName, seat: {municipality, county}, activityCode, profile, source: "sudreg"}`.
6. OIB se ne logira nigdje: ni u klijentu, ni u servisu, ni u handleru (N4). Test: `OutputCaptureExtension` + provjera da log ne sadrži OIB ni za uspjeh, ni za 400/404/502.
7. Testovi: za svaki demo OIB (Microblink, Valamar, Kraš) očekivani profil; neispravan OIB (krivi checksum, 10 znamenki, slova) → 400; ispravan nepoznat OIB → 404; mock klijent baca `ExternalSourceException` → 502.

---

## 3. Odluke (korisnik, 27.10.)

| # | Pitanje | Odluka | Što to znači u kodu |
|---|---|---|---|
| P1 | `common/` već postoji u `feature/pkg-5-ai` | **Preuzeti njihove fileove + dopuniti** | API-1 kopira `ErrorResponse`, `NotFoundException`, `ExternalSourceException`, `InvalidRequestException` doslovno (isti sadržaj = bez konflikta). `GlobalExceptionHandler` se proširuje (502, "polje: razlog", specifična 404 poruka, bez logiranja odbijenih vrijednosti), pa će vlasnik paketa 5 imati konflikt samo u tom fileu. `TooManyRequestsException` i `AdminKeyGuard` ostaju njima. Javiti vlasniku paketa 5. |
| P2 | N+1 uz paginaciju | **`hibernate.default_batch_fetch_size: 100`** | Bez `@EntityGraph` nad kolekcijama. Lista = stranica + count + 1 upit po kolekciji. |
| P3 | 49 natječaja sa `status = null` | **Default `OPEN`; nepoznati samo eksplicitno** | Filter `status` je vlastiti enum `FundingCallStatusFilter { OPEN, ANNOUNCED, CLOSED, UNKNOWN }` (`UNKNOWN` = `status is null`). Facet `status` vraća `UNKNOWN` umjesto `null`. Entitet se ne mijenja. |
| P4 | Sudreg | **OTVORENO** (nije odgovoreno) | Vidi ispod. |
| P5 | Županije | **Normalizacija u kodu** | `common/CountyNames.normalize()` za filter i matching, baza se ne dira. |
| P6 | 404 poruka | **Specifična hrvatska poruka iz iznimke** | `new NotFoundException("Natječaj ne postoji.")`; handler vraća `e.getMessage()`. Odstupa od paketa 5 (oni bacaju engleske poruke za log), pa pri spajanju njihove `NotFoundException` poruke treba prevesti. Javiti vlasniku paketa 5. |
| P7 | `sort` | **`deadline` (asc, prazni na kraju) / `publishedAt` (desc)**, bez parametra smjera | Enum `FundingCallSort`, binding iz `deadline`/`publishedAt` preko malog `Converter<String, FundingCallSort>`. |
| P8 | Pretraga bez dijakritika | **Da, `unaccent`** | Vidi API-2 korak 3. |
| P9 | 400 za profil | **`type` + barem jedna tema** (`areas`/`keywords`/`activityCodes`) | Record-level validacija (`@AssertTrue boolean isHasTopic()` ili vlastita anotacija na klasi). |
| P10 | PR-ovi | **Jedan PR za cijeli paket** | Vidi "Git" na vrhu. |

**P4 (otvoreno), moja preporuka:**
- **Dodati `SUDREG` u `SourceCode`.** Kriterij API-5 "svaki izvor iz enuma jednom" i "uključi Sudreg" se inače ne mogu oba ispuniti. Rizik: `SourceCode` koristi i ingest (paket 2/3). Ako neki kod radi `for (SourceCode s : values())` za scrapanje, mora preskočiti `SUDREG` i `NARODNE_NOVINE`. Provjeriti prije dodavanja.
- **Kopirati fixturee u `src/main/resources/sudreg-fixtures/`**, jer test resursi nisu na runtime classpathu, pa demo bez ključa inače ne radi.
- **API-7 definira `SudregClient` + `CompanyInfo`** po potpisu iz INGEST-9 specifikacije (`name`, `seat.municipality`, `seat.county`, `legalForm`, `activityCode`), a INGEST-9 samo doda pravu implementaciju. Javiti vlasniku paketa 3.

## 4. Koga treba obavijestiti
- **Vlasnik paketa 5 (AI):** P1 i P6 (konflikt u `GlobalExceptionHandler`, poruke `NotFoundException` na hrvatskom).
- **Vlasnik paketa 3 (INGEST-9):** P4 (`SudregClient` sučelje dolazi iz API-7).
- **Web paketi 6 i 7:** JSON primjeri u `work/api-examples.md` na branchu `sime/api`; `status` filter ima i `UNKNOWN`.
