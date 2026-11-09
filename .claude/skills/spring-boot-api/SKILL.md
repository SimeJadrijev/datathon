---
name: spring-boot-api
description: Conventions and rules for writing backend code in apps/api (Spring Boot 4, Java 25, JPA, PostgreSQL). Use whenever you add or change Java code in apps/api - endpoint, service, entity, repository, client for an external API (data.gov.hr, Sudski registar, Narodne novine), data import, error handling or tests.
---

# Spring Boot API (`apps/api`)

Goal: code that works for the demo by 9 Nov, that four people can read without explanation, and that the jury can inspect (criteria "Feasibility" and "Technical architecture"). Simple and consistent beats clever.

## Stack (verified in `pom.xml`)
- Spring Boot 4.1, Java 25, `spring-boot-starter-webmvc` (blocking MVC, NOT WebFlux), Data JPA, Validation, Lombok, PostgreSQL 17 in Docker (`localhost:5433`).
- `ddl-auto: update` and `open-in-view: false` are intentional for the POC phase. Do not introduce Flyway/Liquibase without team agreement.
- Boot 4 uses **Jackson 3** (packages `tools.jackson.*`, not `com.fasterxml.jackson.databind`). Annotations like `@JsonProperty` are still in `com.fasterxml.jackson.annotation`. If unsure about an import, check what is on the classpath; don't guess.
- Do not add dependencies to `pom.xml` without a short justification to the user (what, why, alternative without the dependency).

## Structure: package by feature, not by layer
```
com.datathon.api
├── ApiApplication.java
├── common/              # GlobalExceptionHandler, ErrorResponse, NotFoundException, config
├── fundingcall/          # one feature = one package
│   ├── FundingCallController.java
│   ├── FundingCallService.java
│   ├── FundingCallRepository.java
│   ├── FundingCall.java            # JPA entity
│   └── dto/
│       ├── FundingCallResponse.java
│       └── FundingCallFilter.java
└── integration/
    └── sudreg/          # client for an external API + its DTOs
```
- **All code is in English**: packages, classes, methods, fields, DB tables/columns, JSON fields, URL paths. The Croatian names in `poc/` exist only because it is a throwaway POC; never copy them over. Translate domain terms using the glossary below so the whole team uses the same words.
## Domain glossary (Croatian → code)
| Croatian | Code (English) |
|---|---|
| natječaj / javni poziv / poticaj / stipendija | `FundingCall` (type in `FundingCallType`: `GRANT`, `SUBSIDY`, `SCHOLARSHIP`, `PUBLIC_CALL`...) |
| propis / zakon (Narodne novine) | `Regulation` |
| profil korisnika | `Profile` |
| izvor podataka | `Source` |
| davatelj (tijelo koje raspisuje) | `Provider` |
| udruga | `Association` |
| status (otvoren / zatvoren / najavljen) | `FundingCallStatus`: `OPEN` / `CLOSED` / `ANNOUNCED` |
| rok prijave | `deadline` |
| iznos (ukupni / min / max) | `totalAmount` / `minAmount` / `maxAmount` |
| ciljna skupina | `targetGroup` |
| područje | `area` |
| županija / JLS | `county` / `municipality` |
| djelatnost (NKD) | `activityCode` (NKD) |
| OIB | `oib` (proper Croatian identifier, keep as is) |
| vrsta natječaja (natječaj / poticaj / javni poziv / stipendija / zajam / nagrada) | `FundingCallType`: `GRANT` / `SUBSIDY` / `PUBLIC_CALL` / `SCHOLARSHIP` / `LOAN` / `AWARD` |
| ciljne skupine (građanin, obrt, d.o.o., udruga, JLS, student, poljoprivrednik) | `TargetGroup`: `CITIZEN`, `CRAFT_BUSINESS`, `COMPANY`, `ASSOCIATION`, `LOCAL_GOVERNMENT`, `STUDENT`, `FARMER` |
| cijela država (županije "sve") | `FundingCall.ALL_COUNTIES` = `"ALL"` |
| izvori (udruge.gov.hr, FZOEU, fondovieu, HZZ, APPRRR, MZOM, NN) | `SourceCode`: `ASSOCIATIONS_OFFICE`, `FZOEU`, `EU_FUNDS_NPOO`, `HZZ`, `APPRRR`, `MZOM`, `NARODNE_NOVINE` |
| prilog (dokument natječaja) | `Attachment` (`name`, `url`) |
| sažetak uvjeta / potrebni dokumenti | `conditionsSummary` / `requiredDocuments` |
| vrsta propisa (zakon, uredba, pravilnik, odluka) | `RegulationType`: `LAW`, `DECREE`, `ORDINANCE`, `DECISION`, `OTHER` |
| broj Narodnih novina (npr. 121/2026) | `gazetteNumber` |
| ELI (European Legislation Identifier) | `eliUrl`, `eliMetadata` |
| pokretanje dohvaćanja / zapis po izvoru | `IngestRun` / `SourceRunLog` |
| potrošnja LLM-a (tokeni, cijena) | `LlmUsage` |
Add new terms here before using them in code.

- Don't create `service/impl/` + an interface for every service. Use an interface only when there really are two implementations (e.g. real and mock Sudski registar client).

## Layers and responsibilities
| Layer | May | Must not |
|---|---|---|
| `@RestController` | receive request, `@Valid`, call one service, return a DTO | business logic, call repositories, return entities |
| `@Service` | business logic, `@Transactional`, map entity → DTO, call repositories and clients | know about HTTP (`HttpServletRequest`, `ResponseEntity`) |
| `Repository` (`JpaRepository`) | derived queries, `@Query` (JPQL), `Specification` for filters | logic |
| `integration/*` client | HTTP call to an external API, map their JSON to our DTO | write to the database |

## Class rules
- **Constructor injection only**: `@RequiredArgsConstructor` + `private final` fields. Never field `@Autowired`.
- **DTOs are `record`s**, with Bean Validation annotations on input DTOs:
  ```java
  public record FundingCallFilter(
          @Size(max = 200) String q,
          FundingCallStatus status,
          @Min(0) Integer page) {}
  ```
- **Entities**: `@Getter @Setter @NoArgsConstructor` (protected constructor is fine). **Never `@Data` or `@EqualsAndHashCode` on an entity** (lazy loading, infinite `toString`). Relations `@ManyToOne(fetch = LAZY)`; `@OneToMany` only when really needed.
- ID: `@Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id`. For data from external sources also add a natural key (`source` + `externalId`) with a unique constraint so re-imports don't create duplicates.
- Enums: `@Enumerated(EnumType.STRING)`, never ORDINAL.
- Money: `BigDecimal`; dates: `LocalDate` / `Instant`. Never `double` for amounts, never `java.util.Date`.

## Writing methods
- **One method = one thing.** If the name needs "And" (`fetchAndSave`), split it.
- Short: aim for ~25 lines max. Longer? Extract a private method whose name explains *why*, not *how*.
- **Guard clauses first**, no deep nesting:
  ```java
  public FundingCallResponse getById(Long id) {
      FundingCall fundingCall = fundingCallRepository.findById(id)
              .orElseThrow(() -> new NotFoundException("Natječaj " + id + " ne postoji") // user-facing message stays Croatian);
      return FundingCallResponse.from(fundingCall);
  }
  ```
- Names: verb for actions (`findOpen`, `importFromAssociations`, `matchProfile`), `is/has` for booleans. No abbreviations (`nat`, `res`, `tmp`).
- Never return `null` from public methods: `Optional` for "may be absent", empty list for "no results".
- Parameters: 3-4 max. More than that → a `record` (e.g. `FundingCallFilter`).
- Entity → DTO mapping as a static method on the DTO (`FundingCallResponse.from(entity)`). No MapStruct until there's a real need.
- Use Java 25 where it makes code clearer: `record`, `switch` with pattern matching, `var` only when the type is obvious from the right-hand side. Don't overdo streams: if a stream has more than ~4 steps, a plain `for` is more readable.
- Comments explain **why** (e.g. "FZOEU puts the deadline only in the PDF, hence the LLM"), not *what* the code does. A service's public API may have a short Javadoc when the name isn't enough.

## REST conventions
- All routes under **`/api`** (the frontend `axios` has `baseURL: '/api'`): `@RequestMapping("/api/funding-calls")`.
- Resources plural, lowercase, hyphens: `/api/funding-calls`, `/api/funding-calls/{id}`, `/api/profiles/{oib}/recommendations`.
- List `GET`s always support pagination (`page`, `size`, default `size=20`, max 100) and return a page-like DTO (`content`, `totalElements`, `page`, `size`), not a raw Spring `Page` (its JSON is not stable).
- Statuses: 200 read, 201 create (+ `Location`), 204 delete, 400 validation, 404 not found, 502 when an external source is down.
- JSON fields camelCase, dates ISO-8601 (Jackson handles `LocalDate` by itself).

## Errors: one shape for the whole API
The frontend (`apps/web/src/types/ErrorResponse.type.ts`) expects `{ statusCode, message, error }` and displays `message`. Therefore:
- `common/ErrorResponse.java`: `public record ErrorResponse(int statusCode, String message, String error) {}`
- `common/GlobalExceptionHandler.java` (`@RestControllerAdvice`) maps: `NotFoundException` → 404, `MethodArgumentNotValidException` → 400 (message = first field + reason), `ExternalSourceException` → 502, everything else → 500 with a generic message (details only in the log).
- **`message` is in Croatian** and understandable to end users, because it is shown in the UI. Stack traces never go into the response.
- Don't catch `Exception` in a service just to swallow it. Either handle it meaningfully or let the handler deal with it.

## External APIs (data.gov.hr CKAN, Sudski registar, NN, WP REST)
This is the core of the "Use of open data" criterion, so it must be reliable and visible in the code.
- Client: Spring `RestClient` (or an `@HttpExchange` interface) in `integration/<source>/`, one client per source. **Timeouts are mandatory** (connect ~5 s, read ~30 s).
- Base URL and keys from `application.yaml` via `${ENV_VAR}` and a `@ConfigurationProperties` record. **Keys (Sudreg OAuth, ANTHROPIC_API_KEY) never in code or in a commit**; locally in `.env` / environment variables.
- Map their JSON to a dedicated `record` inside the client package, and let only our domain DTO into the rest of the app. Foreign field names don't leak into services.
- Wrap external source errors in `ExternalSourceException` with the source name. One failing source must not break the whole import: log, skip, continue.
- Be polite: at most ~1 request/s per domain (as in the POC), `User-Agent` with the project name.
- For the demo: store import results in the database and read from the database. The demo must not depend on someone else's server being up while recording the video.
- Parsing logic in `poc/` (Python) is the reference for behaviour (fields, per-source quirks). Before writing a new parser, look at the matching module in `poc/sources/` and `poc/docs/izvori.md`.

## Transactions and performance
- `@Transactional(readOnly = true)` on read-only service methods, `@Transactional` on writing ones. Not on controllers.
- Watch for N+1: for lists that need a related entity use `@EntityGraph` or `join fetch`.
- Filters with many optional fields go through `Specification` or a single `@Query` with `(:param IS NULL OR ...)`, not 8 derived methods.
- Don't load a whole table into memory to filter in Java.

## Logging
- `@Slf4j` (Lombok). `info` for import start/end with numbers ("udruge.gov.hr: 97 open, 3 skipped"), `warn` for skipped records, `error` with the exception for the unexpected. Never `System.out.println`.
- Don't log personal OIBs of natural persons or API keys.

## Tests (competition level)
- We don't test every getter. We test what breaks and is hard to check by hand:
  1. parsers / mapping of external responses (unit test with a real saved JSON/HTML sample in `src/test/resources/fixtures/`),
  2. profile-to-funding-call matching (unit),
  3. one `@WebMvcTest` per controller for the happy path + 404/400.
- Tests never call real external services.

## Before saying it's done
1. `./mvnw -q compile` passes (Windows: `mvnw.cmd -q compile`); if you touched logic that has tests, also `./mvnw test`.
2. If the API is running, check the endpoint with `curl` and show the real response.
3. New endpoint used by the frontend: tell the user the exact JSON shape so the type in `apps/web/src/types/` can be aligned (or add it yourself following the `react-web` skill).
4. A new architecture decision (new dependency, new source, model change) goes into the PR description with the reasoning.
