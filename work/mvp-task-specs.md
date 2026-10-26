# MVP work packages (for GitHub issues)

_Version: 2026-10-26 (v3: 8 work packages, simple English). Each package below is one GitHub issue. The team decides internally who takes which package. The steps inside a package keep their IDs (for example `API-2`) and can be given to an AI agent one by one. Sizes are in days of work for one person. Decisions and reasons: `work/decisions.md`. Status: `work/tasks.md`._

_Language note: issue text is in English on purpose. All code is in English too. Only text that users see in the app (UI labels, error messages from the API) is in Croatian._

## 0. Common context (applies to EVERY package and step)

Every issue must link to this file. An agent that gets one package or one step must read this section first.

### 0.1 What we build
A web app that shows funding calls (grants, subsidies, scholarships, public calls) and new laws in Croatia in one place. The user enters a short profile (or a company OIB). The app shows the calls that fit, explains why each one fits, gives an AI summary of the conditions, and answers "Do I meet the conditions?". Competition: Datathon of the Open Data Portal 2026, **challenge C** (Economy and efficient governance: save time and cost of finding information, connect public data).

### 0.2 Dates
- Today is 26.10.2026. **Internal deadline for a working prototype: 6.11.** Submission by email: **9.11.2026 at 24:00** (code, documentation, demo material, video up to 3 min). No video = not scored.

### 0.3 How we are scored (max 60 points) and what it means for the work
| Criterion | Points | What the jury must see |
|---|---|---|
| Feasibility | 10 | Runs from a clean checkout: `docker compose up`, API, web. Data is in our database, not on someone else's server |
| UI/UX | 10 | Four screen states (loading, error, empty, data), Croatian text, simple flow |
| Video | 10 | The demo works every time and looks good at 1280 px |
| Use of open data | 5 | Data taken straight from APIs (Sudreg, Narodne novine ELI, CKAN), source visible next to each item |
| Connecting data | 5 | Several sources joined: calls + Sudreg + NKD + laws |
| Innovation | 5 | AI summary and eligibility check, explanation of the match |
| Architecture | 5 | Clear architecture, OpenAPI, listed prerequisites |
| Relevance, Impact | 5 + 5 | Story: saves time for citizens, craftsmen, associations |

No prize below 50 points. So "works reliably and looks tidy" matters more than the number of sources.

### 0.4 Stack and rules (must read)
- Monorepo: `apps/api` (Spring Boot 4.1, Java 25, MVC, JPA, PostgreSQL 17 on `localhost:5433`) and `apps/web` (React 19, TypeScript, Vite, wouter, TanStack Query, axios, yarn).
- **Before writing code, load the skill:** `.claude/skills/spring-boot-api/SKILL.md` for `apps/api`, `.claude/skills/react-web/SKILL.md` for `apps/web`. They define layers, DTO records, the error shape `{statusCode, message, error}`, the four screen states, barrel `index.ts` files and the `@/` alias.
- **All code is in English.** Only UI text and user-facing error messages are Croatian. Croatian names from `poc/` are never copied into code.
- **Do not add dependencies** to `pom.xml` or `package.json` without a short reason in the PR (what, why, alternative). Pre-approved: Jsoup, Apache PDFBox, springdoc-openapi, and the Claude API client (official Java SDK if it exists, otherwise `RestClient`).
- `ddl-auto: update` stays (no Flyway). Entities: `@Getter @Setter @NoArgsConstructor`, never `@Data`. Natural key `source + externalId` with a unique constraint.
- Secrets (`ANTHROPIC_API_KEY`, `SUDREG_CLIENT_ID`, `SUDREG_CLIENT_SECRET`, `ADMIN_KEY`) come only from environment variables. Never in code, commits or logs.
- Before saying "done": `mvnw.cmd -q compile` (and tests) for the API, `yarn lint` and `yarn build` for the web. A new architecture decision goes into `work/decisions.md`.

### 0.5 Work package and pull request rules
- The work is split into **8 work packages**. One package = one GitHub issue = one person (assigned by the team). That person may open one PR for the whole package or a few PRs (for example one per step). Review size is not a concern. Avoiding merge conflicts is.
- Steps keep their original IDs (for example `API-2`) because other steps refer to them. Each step has its own acceptance criteria. A package is done when all its steps are done.
- Branch name: `feature/pkg-<number>-short-name`, for example `feature/pkg-4-api`.
- The PR description links the issue and shows proof: `curl` output for API steps, screenshots (all four states) for web steps, a comparison table for ingest steps.
- Do not commit: `.idea/`, `.env`, `data/cache/`, `node_modules`, `target`.
- **Each person works in their own folders:** ingest (`ingest/`, `integration/sudreg/`), API (feature packages `fundingcall/`, `regulation/`, `profile/`, `common/`), AI (`integration/llm/`, `ai/`), web (`apps/web`).
- **Shared files** (`pom.xml`, `application.yaml`, `.gitignore`, `routes.ts`, `Router.tsx`, barrel `index.ts` files) cause most merge conflicts. Dependencies and empty config blocks are added once, in DATA-1. All routes and placeholder pages are added once, in WEB-1. Later steps only fill in their own files. If you must change a shared file, make it a small separate commit and merge it first.

### 0.6 POC as reference (Python, not in `main`)
The POC is on the branch `origin/poc/natjecaji-scraper`, folder `poc/`. Read it without checkout: `git show origin/poc/natjecaji-scraper:poc/sources/udruge.py`. List files: `git ls-tree -r --name-only origin/poc/natjecaji-scraper`. Most useful:
- `poc/docs/izvori.md`: structure of each source, robots.txt, selectors, quirks. **Read before writing a parser.**
- `poc/sources/*.py` (one module per source), `poc/sources/base.py` (attachments, `looks_like_funding`), `poc/common/http.py` (robots.txt, 1 request/s per domain, cache), `poc/extract/rules.py` (regex for deadline, amount, groups, areas), `poc/extract/llm.py` (JSON schema and prompt), `poc/extract/pdf.py`, `poc/match.py` (scoring), `poc/opendata/sudreg.py` and `nkd.py`, `poc/sources/nn.py`.
- `poc/IZVJESTAJ.md` and `poc/docs/zakljucak.md`: numbers, risks, standard proposal.
- It is a reference for behaviour, not code to translate line by line. The Java version must give similar numbers.
- Warning: that branch has `.idea/` and `.claude/launch.json` committed by mistake. Do not copy them.

### 0.7 Known facts about the data (measured in the POC, 24.10.2026)
- The POC database had 1,485 calls from 11 sources (about 185 open) and 3,842 regulations from Narodne novine (169 laws).
- Field completeness (records with details, n=856, before LLM): status 55 %, deadline 41 %, total amount 21 %. NKD codes are almost empty (2-5 %). **Never promise in the UI or video what the data does not have.**
- The LLM (Haiku) processed only 300 of 1,485 records. Accuracy was never measured by hand.
- The rule-based target groups and areas are too wide (for example textbooks tagged "student"). There are duplicates between sources and noise (news, job ads).
- "Who got the money" is out of scope: the open data on association funding ends in 2016.

### 0.8 Domain glossary (Croatian word to code name)
Use the same words everywhere. A new term goes first into the glossary in the `spring-boot-api` skill.
| Croatian (POC) | Code (English) |
|---|---|
| natječaj, poziv, poticaj, stipendija | `FundingCall` |
| type: natječaj / poticaj / javni poziv / stipendija / zajam / nagrada | `FundingCallType`: `GRANT` / `SUBSIDY` / `PUBLIC_CALL` / `SCHOLARSHIP` / `LOAN` / `AWARD` |
| status: najavljen / otvoren / zatvoren | `FundingCallStatus`: `ANNOUNCED` / `OPEN` / `CLOSED` |
| target groups: gradjanin, obrt, doo, udruga, jls, student, poljoprivrednik | `TargetGroup`: `CITIZEN`, `CRAFT_BUSINESS`, `COMPANY`, `ASSOCIATION`, `LOCAL_GOVERNMENT`, `STUDENT`, `FARMER` |
| areas: okolis, energetika, promet, poljoprivreda, obrazovanje, znanost, kultura, sport, socijala, zdravstvo, zaposljavanje, poduzetnistvo, inovacije, digitalizacija, turizam, mladi, civilno_drustvo | `Area`: `ENVIRONMENT`, `ENERGY`, `TRANSPORT`, `AGRICULTURE`, `EDUCATION`, `SCIENCE`, `CULTURE`, `SPORT`, `SOCIAL`, `HEALTH`, `EMPLOYMENT`, `ENTREPRENEURSHIP`, `INNOVATION`, `DIGITALIZATION`, `TOURISM`, `YOUTH`, `CIVIL_SOCIETY` |
| sources | `SourceCode`: `ASSOCIATIONS_OFFICE` (udruge.gov.hr), `FZOEU`, `EU_FUNDS_NPOO` (fondovieu.gov.hr), `HZZ`, `APPRRR`, `MZOM`, `NARODNE_NOVINE` |
| propis, zakon | `Regulation`, `RegulationType` (`LAW`, `DECREE`, `ORDINANCE`, `DECISION`, `OTHER`) |
| rok prijave, davatelj, uvjeti | `deadline`, `provider`, `conditionsSummary` |

---

## 1. Work packages overview

Create one GitHub issue per package by copying the whole package section from this file.

| Pkg | Title | Steps | Days | Needs from other packages |
|---|---|---|---|---|
| 1 | Foundations | SETUP-1, DATA-1, DATA-2 | 2.2 | none (everything else waits for DATA-1, so merge this package first) |
| 2 | Ingest: core and first sources | INGEST-1 to INGEST-4 | 3.3 | package 1 (DATA-1) for INGEST-2 |
| 3 | Ingest: other sources | INGEST-5 to INGEST-9 | 3.1 | package 2 (INGEST-1, INGEST-2) and SETUP-1 for INGEST-9 |
| 4 | API | API-1 to API-7 | 4.8 | package 1 (DATA-1, DATA-2); API-7 also needs INGEST-9 or the fixtures from SETUP-1 |
| 5 | AI | AI-1 to AI-4 | 3.2 | package 1 (DATA-1); AI-2 needs INGEST-2, AI-3 needs INGEST-8, AI-4 needs API-2 |
| 6 | Web: foundation, profile and recommendations | WEB-1 to WEB-4 | 3.2 | none for WEB-1 and WEB-2; WEB-3 needs API-7; WEB-4 needs API-6 |
| 7 | Web: lists, details, laws and sources | WEB-5 to WEB-9 | 3.9 | package 6 (WEB-1, WEB-4); API-2 to API-5; AI-4 for WEB-7 |
| 8 | Quality and delivery | QUALITY-1 to DELIVERY-5 | 6.2 | API-2, the INGEST steps, AI-2 and the web packages; DELIVERY-1 can start now |

Total: 40 steps, about 29.9 days of work. With 4 people and about 12 working days left (about 48 person-days, minus meetings, reviews and fixes) this is possible but tight.

**If we fall behind on 1.11., cut in this order:** WEB-9, API-3 (filter counts), INGEST-7 (MZOM), AI-3 (regulation summaries). Work not needed for the demo is in "Nice-to-have" near the end.

---

# Package 1: Foundations

**Size:** 2.2 d | **Needs from other packages:** none (everything else waits for DATA-1, so merge this package first)

**Goal:** Create the shared base everything else needs: the Sudreg key, the database model, the shared build files and the seed data.

**Steps, in this order:**
1. `SETUP-1` Register Sudreg key and pick demo OIBs (0.2 d)
2. `DATA-1` Database entities, enums and shared build files (1.0 d)
3. `DATA-2` Seed snapshot: export from POC and load on startup (1.0 d)

**The package is done when** every step below is done and its acceptance criteria are checked.

## SETUP-1: Register Sudreg key and pick demo OIBs

**Size:** 0.2 d | **Depends on:** none | **Blocks:** INGEST-9, API-7, WEB-3, DELIVERY-2

**Why:** The criterion "Use of open data" rewards a live API. The POC used a fake OIB (`00000000001`) with a mock file, which loses points and does not prove that the chain OIB to business activity works. The Sudreg API needs OAuth2 client credentials (free registration, the key arrives right after email confirmation).

**What to do:**
1. Register at https://sudreg-data.gov.hr/ and get `client_id` and `client_secret`. OpenAPI docs: https://sudreg-data.gov.hr/api/javni/dokumentacija/open_api. Token endpoint: `https://sudreg-data.gov.hr/api/oauth/token`.
2. Keep the key in a local `.env` (git-ignored) or environment variables `SUDREG_CLIENT_ID` and `SUDREG_CLIENT_SECRET`. Share inside the team with a password manager, never through the repo or chat.
3. Test by hand (curl or Postman): `GET /api/javni/detalji_subjekta?tip_identifikatora=oib&identifikator=<OIB>&expand_relations=true` with a Bearer token.
4. Pick **2-3 real OIBs of companies** for the demo (public company data, never OIBs of private persons). Suggestion: one IT d.o.o., one manufacturer or tourism company, one association or craftsman if Sudreg has it. Check that `glavna_djelatnost` (main activity) is not empty.
5. Save the response of each chosen OIB as a test fixture: `apps/api/src/test/resources/fixtures/sudreg/<oib>.json`.

**Notes:**
- `glavna_djelatnost` is an NKD 2007 code without the dot (`1072` means `10.72`, `150` means `01.50`). The field `pretezita_djelatnost` is empty for public users. See `poc/opendata/sudreg.py`, function `nkd_iz_glavne`.
- Rate limit is not documented. Stay under about 1 request per second.

**Acceptance criteria:**
- [ ] A token is received and `detalji_subjekta` returns 200 for all chosen OIBs.
- [ ] For each OIB the issue comment has a table: name, seat (municipality, county), NKD code, legal form.
- [ ] Fixture JSON files are in the repo and **no key is anywhere in the repo** (check `git diff` before commit).
- [ ] The issue comment says who holds the key and where it is stored.

**Out of scope:** writing the Java client (INGEST-9).

---

## DATA-1: Database entities, enums and shared build files

**Size:** 1 d | **Depends on:** none | **Blocks:** DATA-2, API-1, INGEST-2, AI-1

**Why:** Every other step needs the same data model. Doing it once, early and small, lets the API, ingest and AI work in parallel.

**What to do (in `apps/api`, package by feature, see the `spring-boot-api` skill):**
1. Enums from the glossary in section 0.8: `SourceCode`, `FundingCallType`, `FundingCallStatus`, `TargetGroup`, `Area`, `RegulationType`.
2. Entity `fundingcall/FundingCall`:
   - `id` (Long, IDENTITY), `source` (`SourceCode`) + `externalId` (unique together), `url`, `title`, `provider`, `type`, `status`, `publishedAt` (LocalDate), `deadline` (LocalDate, nullable), `deadlineNote` (String, for example "until funds run out"), `totalAmount`, `minAmount`, `maxAmount` (`BigDecimal`, EUR, nullable).
   - `targetGroups` (`Set<TargetGroup>`), `areas` (`Set<Area>`), `counties` (`Set<String>`, value `ALL` for the whole country), `activityCodes` (`Set<String>`, NKD), all as `@ElementCollection` with STRING enums.
   - `conditionsSummary` (TEXT), `requiredDocuments` (`List<String>`), `attachments` (`List<Attachment>`, embeddable with `name` and `url`), `eligibilityQuestions` (JSON or element collection, filled by AI-4).
   - `rawTextHash`, `extractedByLlm` (boolean), `aiFields` (`Set<String>`, names of fields set by AI), `fieldsReviewed` (boolean), `hidden` (boolean) and `hiddenReason`, `duplicateOf` (Long, nullable), `fetchedAt` (Instant), `sourceText` (TEXT, raw text for the LLM, never sent by the API).
3. Entity `regulation/Regulation`: `id`, `externalId` (for example `2026-121-1434`, unique), `title`, `type`, `gazetteNumber` (for example `121/2026`), `documentNumber`, `publishedAt`, `enactedAt` (nullable, from ELI), `url`, `eliUrl`, `pdfUrl`, `areas`, `summary` (TEXT, nullable, filled by AI-3), `eliMetadata` (boolean).
4. Entities `ingest/IngestRun` and `ingest/SourceRunLog` (`startedAt`, `source`, `fetched`, `skipped`, `errors`, `durationMs`) and `ai/LlmUsage` (`model`, `inputTokens`, `outputTokens`, `costUsd`, `purpose`, `createdAt`).
5. Spring Data repositories with the derived queries the API will need (find by `source + externalId`, find by status).
6. Add every new term to the glossary in the `spring-boot-api` skill.
7. **Shared build files (do this once, here):** add all pre-approved dependencies to `pom.xml` (Jsoup, Apache PDFBox, `springdoc-openapi-starter-webmvc-ui`, and the Claude API client if an official Java SDK exists) and add empty config blocks to `application.yaml`: `seed:`, `ingest:`, `llm:` (with `budget-usd`), `sudreg:`, `admin:`. Add `data/cache/` to `.gitignore`. This way later steps do not all edit the same lines and cause merge conflicts. Give the reason for each dependency in the PR.

**Acceptance criteria:**
- [ ] The app starts on an empty Postgres and `ddl-auto: update` creates all tables (`docker compose up -d` then `mvnw.cmd spring-boot:run`).
- [ ] The unique constraint on `source + externalId` is enforced (small test).
- [ ] `pom.xml`, `application.yaml` and `.gitignore` already contain what later steps need.
- [ ] `mvnw.cmd -q compile` passes.

**Out of scope:** REST endpoints, loading data, Flyway.

---

## DATA-2: Seed snapshot: export from POC and load on startup

**Size:** 1 d | **Depends on:** DATA-1 | **Blocks:** API-2, API-4, API-5, QUALITY-1

**Why:** The frontend and API must not wait for the Java scrapers. The POC already collected about 1,500 calls and 3,842 regulations. A snapshot committed to the repo makes the demo repeatable from a clean checkout (Feasibility, 10 points) and independent of other people's servers. The POC review found that the POC demo cannot run from the repo, because `data/` and `web/data/` are git-ignored.

**What to do:**
1. Write a one-time export script `tools/export_poc_snapshot.py`. It reads the POC SQLite file (`poc/data/natjecaji.sqlite`, tables `natjecaji` and `propisi`) and writes `apps/api/src/main/resources/seed/funding-calls.json` and `regulations.json`. (Python is fine here. It is a one-time tool, not part of the product.)
   - Map Croatian values to the English enums (glossary in 0.8).
   - **Skip** records from sources outside the MVP: `rijeka`, `osijek`, `zagreb`, `ampeu`, `hamag`.
   - Write `snapshotDate` (ISO date of the export) into the files.
   - Drop `sourceText` for closed calls older than 12 months (keeps the repo small).
2. Write a Spring `ApplicationRunner` that loads the seed when the tables are empty. It must be safe to run twice (upsert by `source + externalId`). Switch with the property `seed.enabled` (default true).
3. To get the POC database: run the POC (`poc/README.md`, first run about 10 minutes), or ask the colleague who wrote it to send `natjecaji.sqlite`.

**Acceptance criteria:**
- [ ] On an empty database, one start fills the tables with no manual step.
- [ ] A second start creates no duplicates (same row counts).
- [ ] The log prints the number of imported calls per source and the number of regulations. Put these numbers in the PR.
- [ ] No record has a source outside the MVP set.
- [ ] Unit test: one POC record from a fixture is mapped to an entity, including a null deadline and counties `["sve"]` becoming `ALL`.
- [ ] The snapshot files are committed and the total size is reasonable (under about 15 MB).

**Out of scope:** REST endpoints, Java scrapers.

---

# Package 2: Ingest: core and first sources

**Size:** 3.3 d | **Needs from other packages:** package 1 (DATA-1) for INGEST-2

**Goal:** Build the polite HTTP client and the ingest framework, then the first two sources (Associations Office and FZOEU). After this, every other source only adds one parser.

**Steps, in this order:**
1. `INGEST-1` Polite HTTP client: robots.txt, rate limit, cache (0.8 d)
2. `INGEST-2` Ingest framework: fetcher interface, rules, filter, upsert, run log, admin trigger (1.0 d)
3. `INGEST-3` Source: Associations Office (udruge.gov.hr) (0.7 d)
4. `INGEST-4` Source: FZOEU with PDF reading (0.8 d)

**Decision (26.10.):** everything is in one stack (Java). The POC scrapers are small (50-90 lines each) and are rewritten with Jsoup and PDFBox. One stack means one Docker setup and one schema, and fewer things that can break while we record the video. The POC stays as the reference.

**Rules for every source step:**
- Read `poc/docs/izvori.md` and `poc/sources/<source>.py` first.
- **Comparison with the POC is mandatory:** the PR has a table of records fetched by Java vs the POC (`izvori_log`). A difference over 10 % must be explained.
- Each parser has a unit test on a **saved HTML/JSON sample** in `src/test/resources/fixtures/<source>/`. Tests never call a real server.
- Out of scope for all: Zagreb, Rijeka, Osijek, AMPEU, HAMAG (dropped: expensive to maintain, low coverage), the Narodne novine ads section, EOJN, LLM extraction (AI-2), scheduler.

**The package is done when** every step below is done and its acceptance criteria are checked.

## INGEST-1: Polite HTTP client: robots.txt, rate limit, cache

**Size:** 0.8 d | **Depends on:** none | **Blocks:** INGEST-2, INGEST-9

**Why:** Every source goes through this one client. It makes sure we respect the sites we read (robots.txt, at most 1 request per second per domain). This also protects the project: some sources forbid certain paths, for example `fondovieu.gov.hr/download`. Reference: `poc/common/http.py`.

**What to do:**
1. `PoliteHttpClient` (one shared instance, Spring `RestClient`). User-Agent: `DatathonBot/0.1 (studentski projekt)`.
2. **robots.txt check:** a small own parser for `Disallow` lines for our agent and `*` (no new dependency). A forbidden URL throws `RobotsBlockedException`, which callers log and skip.
3. **Rate limit:** at most 1 request per second per domain, also when several sources run in parallel.
4. Timeouts: connect 5 s, read 30 s. Three tries for 429/502/503/504, with growing waits.
5. Decode UTF-8 and fall back to `windows-1250` (see `decode()` in the POC).
6. **Disk cache** in `data/cache/` (add to `.gitignore`): HTML for 24 hours, PDF forever. A `refresh` option skips the cache.
7. Methods: `getText(url)`, `getJson(url)`, `getBytes(url)`.

**Acceptance criteria:**
- [ ] Test: a path disallowed in a sample robots.txt is not fetched (use a local test server, for example `MockWebServer` or the JDK `HttpServer`; no new dependency if avoidable).
- [ ] Test: 5 requests to the same domain take at least 4 seconds.
- [ ] Test: the second call to the same URL is served from the cache.
- [ ] Test: a `windows-1250` response is decoded correctly.

**Out of scope:** parsing, database.

---

## INGEST-2: Ingest framework: fetcher interface, rules, filter, upsert, run log, admin trigger

**Size:** 1 d | **Depends on:** DATA-1, INGEST-1 | **Blocks:** INGEST-3 to INGEST-8, AI-2

**Why:** All source steps plug into one framework. Doing it first keeps each source PR small (about one parser and one test).

**What to do (package `com.datathon.api.ingest`):**
1. Interface `SourceFetcher`: `SourceCode code()`, `List<RawItem> fetchList()`, `RawItem fetchDetail(RawItem item)`.
2. Record `RawItem`: `externalId`, `url`, `title`, `provider`, `publishedAt`, `statusHint`, `deadlineHint`, `typeHint`, `categories`, `countiesHint`, `text`, `attachments`, `extra`.
3. `FundingFilter`: port `looks_like_funding` with `NOT_FUNDING_RE` and `FUNDING_RE` from `poc/sources/base.py`. It removes job ads, procurement, rentals and similar noise.
4. `RuleExtractor`: port the regex rules from `poc/extract/rules.py`: deadline, amounts in EUR, target groups, areas. Known POC flaw: groups and areas are too wide, so when unsure return an empty set (AI-2 fills the gap).
5. `IngestService`: for each registered `SourceFetcher`: list, filter, detail, rules, **upsert by `source + externalId`**. Update a record only when `rawTextHash` changed. **A failing source must not stop the others:** catch, log, write the error to `SourceRunLog`, continue.
6. Write one `SourceRunLog` per source and run (`fetched`, `skipped`, `errors`, `durationMs`).
7. Admin route `POST /api/admin/ingest?source=FZOEU`, protected with the header `X-Admin-Key` (value from env `ADMIN_KEY`, hidden from Swagger). It runs asynchronously (`@Async`, answers 202 with a run id), because a full run takes minutes. Ingest on startup is off by default.
8. Add an in-memory `FakeSourceFetcher` for tests.

**Acceptance criteria:**
- [ ] Test: two runs of the same fake source create no duplicates.
- [ ] Test: a source that throws does not stop the other source, and its error is in `SourceRunLog`.
- [ ] Test: `FundingFilter` rejects at least 10 sample job-ad and procurement titles and accepts at least 10 funding titles (taken from the POC data).
- [ ] The admin route returns 401 without the key.
- [ ] `mvnw.cmd -q compile` and tests pass.

**Out of scope:** real sources, LLM.

---

## INGEST-3: Source: Associations Office (udruge.gov.hr)

**Size:** 0.7 d | **Depends on:** INGEST-2 | **Blocks:** QUALITY-2

**Why:** The most valuable source. It is the central list of calls for associations from all providers (ministries, cities, municipalities). The detail page has structured fields: county, deadline, total, minimum and maximum amount. Reference: `poc/sources/udruge.py`.

**What to do:**
1. List page: `https://udruge.gov.hr/istaknute-teme/financiranje-programa-i-projekata-udruga-iz-javnih-izvora/natjecaji-5503/5503?trazi=1&status=1` (open, about 97 items) and `status=2` (closed, about 3,250 items), pagination `page=N`. Items are in `div.news_item`.
2. Fetch **all open** calls. For closed calls fetch only the first N pages (configurable, default 5).
3. Detail page `?nid=<id>`: institution, level, county, title, publish date, **closing date**, **total / minimum / maximum amount**, link to the original call. Map them to `FundingCall`. Target group is `ASSOCIATION` for all.
4. Set `type` and `status` from the page.

**Acceptance criteria:**
- [ ] Comparison table with the POC (expect about 97 open).
- [ ] At least 90 % of open calls have a deadline.
- [ ] Parser tests on saved list and detail HTML.

**Out of scope:** the "who received the money" datasets.

---

## INGEST-4: Source: FZOEU with PDF reading

**Size:** 0.8 d | **Depends on:** INGEST-2 | **Blocks:** QUALITY-2

**Why:** The best demo example (subsidy for electric cars, solar panels). The deadline and amount are often only inside the PDF, so this step also adds PDF reading, which AI-2 reuses. Reference: `poc/sources/fzoeu.py`, `poc/extract/pdf.py`.

**What to do:**
1. Source page: `https://www.fzoeu.hr/hr/nacionalni-javni-pozivi-i-natjecaji/1367`. **Do not use `/hr/natjecaji/7`** from the old POC brief: it redirects to a wrong page. Items are accordion rows (`li.accordion-header` and `li.accordion-hidden`) with title, status (`div.status`), "Opened: date", description and a documents list. The folder id in `/docs/<ID>/` is a stable `externalId`.
2. `PdfTextExtractor` with Apache PDFBox: up to 3 PDFs per call, first 8 pages, cache with the HTTP client. Put the PDF text in `RawItem.text`.
3. robots.txt forbids two specific PDFs: the HTTP client already skips them.
4. Add the attachments (name and URL) to the record.

**Known trap (check it in a test):** the call for electric cars (EnU-2/26) is read by the POC rules as 2,500 EUR and deadline 31.3.2028. The correct data is 9,000 EUR per vehicle and **no application deadline** (2028 is the end of the purchase period). The rule extractor must not invent a deadline from such a date. If rules cannot decide, leave it empty for AI-2.

**Acceptance criteria:**
- [ ] Comparison table with the POC.
- [ ] PDF extraction test on a saved sample PDF.
- [ ] The EnU-2/26 sample does not produce a deadline of 31.3.2028.
- [ ] Parser test on a saved HTML sample.

**Out of scope:** the closed-calls page and the XLSX plan of calls (nice-to-have).

---

# Package 3: Ingest: other sources

**Size:** 3.1 d | **Needs from other packages:** package 2 (INGEST-1, INGEST-2) and SETUP-1 for INGEST-9

**Goal:** Add the remaining sources: EU funds NPOO, HZZ, APPRRR, MZOM, Narodne novine and the Sudreg client. Each source is an independent parser, so the order is free.

**Steps, in this order:**
1. `INGEST-5` Source: EU funds NPOO (fondovieu.gov.hr) (0.5 d)
2. `INGEST-6` Sources: HZZ and APPRRR (WordPress REST) (0.6 d)
3. `INGEST-7` Source: MZOM scholarships (0.5 d)
4. `INGEST-8` Narodne novine regulations with ELI (0.8 d)
5. `INGEST-9` Sudreg client and NKD mapping (0.7 d)

**Note:** The rules from package 2 (comparison with the POC, saved fixtures, out-of-scope list) apply to every source in this package too.

**The package is done when** every step below is done and its acceptance criteria are checked.

## INGEST-5: Source: EU funds NPOO (fondovieu.gov.hr)

**Size:** 0.5 d | **Depends on:** INGEST-2 | **Blocks:** QUALITY-2

**Why:** Official EU calls with a deadline in almost all records. Reference: `poc/sources/fondovieu.py`.

**What to do:**
1. The site is a server-rendered Nuxt app, so the HTML list has all data without JavaScript. Pagination: `?page=N&statusGroup=otvoreni` (or `zatvoreni`, `svi`), 10 per page. Card: call code, title, call type, deadline.
2. Detail `/pozivi/<id>`: code, publish date, deadline, responsible body, purpose, eligible applicants.
3. **robots.txt forbids `/download`, `/prijavitelj`, `/korisnik`, `/registracija`. Do not download attachments. Only link to them.**
4. Do not use the internal API (not documented publicly).

**Acceptance criteria:**
- [ ] Comparison table with the POC. Deadline filled for nearly all records.
- [ ] Test: a `/download` URL is not fetched (robots.txt).
- [ ] Parser tests on saved list and detail HTML.

**Out of scope:** eufondovi.gov.hr (programme 2021-2027), nice-to-have.

---

## INGEST-6: Sources: HZZ and APPRRR (WordPress REST)

**Size:** 0.6 d | **Depends on:** INGEST-2 | **Blocks:** QUALITY-2

**Why:** Two sources with a public REST API: employment measures (HZZ) and agriculture (APPRRR). They are simple, so one PR covers both. Reference: `poc/sources/hzz.py`, `poc/sources/apprrr.py`.

**What to do:**
1. **HZZ** (`mjere.hzz.hr`, `mjere.hr` redirects there): WordPress REST, post type `measures` (about 97 measures since 2021), taxonomies `measures_years` and `measures_category` (employer, unemployed, self-employment). The full text is in `content`. Measures are continuous: no deadline and no total amount, so set `deadlineNote` to "continuous".
2. **APPRRR** (`apprrr.hr`): REST `/wp-json/wp/v2/posts?categories=155&per_page=100` (category "calls") and the RSS feed `/feed/` (only the 10 latest posts) for fresh items. A post is only an announcement. The documents are on the intervention page that the post links to. Fetch that page and read the PDF links from it.
3. Target group for APPRRR is `FARMER`. For HZZ use the category (employer gives `COMPANY`, craft business, unemployed gives `CITIZEN`).

**Acceptance criteria:**
- [ ] Comparison table with the POC for both sources.
- [ ] Parser tests on saved JSON samples.
- [ ] HZZ records have `deadlineNote = continuous` and no invented deadline.

**Out of scope:** HAMAG-BICRO (its REST API is off, dropped).

---

## INGEST-7: Source: MZOM scholarships

**Size:** 0.5 d | **Depends on:** INGEST-2 | **Blocks:** QUALITY-2

**Why:** Scholarships are the most useful result for the "student" persona in the demo. Reference: `poc/sources/mzom.py`.

**What to do:**
1. List: `https://mzom.gov.hr/istaknute-teme/natjecaji-196/196?trazi=1&page=N` (about 1,550 items, 155 pages). Item: title, **"Rok:"** (deadline), date, type, theme. Default limit: first N pages (configurable).
2. The list mixes in job ads, so **`FundingFilter` is required**. Keep only items about scholarships, calls and funding.
3. Optionally the page "State scholarships" (`/.../drzavne-stipendije/162`).
4. Target group `STUDENT`, type `SCHOLARSHIP` for scholarship items.

**Acceptance criteria:**
- [ ] Comparison table with the POC.
- [ ] Deadline filled for open items that show "Rok:".
- [ ] A saved sample with job ads: none of them become funding calls (test).

**Out of scope:** AMPEU (dropped, overlaps with MZOM).

---

## INGEST-8: Narodne novine regulations with ELI

**Size:** 0.8 d | **Depends on:** INGEST-2 | **Blocks:** AI-3, API-4 (for live data)

**Why:** Laws are an equal part of the product. Narodne novine also gives official ELI metadata (the European standard for legal acts), which is a strong "open data" argument. Reference: `poc/sources/nn.py`.

**What to do:**
1. List URL: `https://narodne-novine.nn.hr/search.aspx?sortiraj=4&kategorija=1&godina=<YEAR>&rpp=200&qtype=1&pretraga=da&str=<N>`. **`str` is the page index starting at 0.** Items are `div.searchListItem`. The title link is in `.resultTitle a` and has a hidden `span.score` that **must be removed before reading the text**. Metadata looks like "NN 121/2026, (1434), odluka, 23.10.2026." (regex in `poc/sources/nn.py`).
2. Current year and previous year. Map the type (law, decree, ordinance, decision) to `RegulationType`.
3. ELI: `/eli/sluzbeni/<year>/<NN number>/<document>` and `/pdf`. For the newest 60 laws fetch the act page and read the ELI RDFa (`date_document`, `date_publication`, `type_document`). Set `eliMetadata = true`.
4. A first guess of `areas` from the title (port `PODRUCJA_RX` from the POC rules).
5. robots.txt forbids the ads section (`/clanci/oglasi/`) and some old acts. The HTTP client skips them.

**Acceptance criteria:**
- [ ] About 3,800 regulations for 2025-2026 (compare with the POC: 3,842, 169 laws).
- [ ] The hidden `span.score` is not in any title (test).
- [ ] ELI metadata filled for the 60 newest laws.
- [ ] Parser tests on saved list and act HTML.

**Out of scope:** the ads section, full text storage, summaries (AI-3).

---

## INGEST-9: Sudreg client and NKD mapping

**Size:** 0.7 d | **Depends on:** SETUP-1, INGEST-1 | **Blocks:** API-7 (real data)

**Why:** The live call to the Court Register is the main proof for the criterion "Use of open data" (straight from an API). Reference: `poc/opendata/sudreg.py`, `poc/opendata/nkd.py`.

**What to do (package `integration/sudreg/`):**
1. `SudregClient` interface and the real implementation: OAuth2 client credentials, token cached until it expires, `GET /api/javni/detalji_subjekta?tip_identifikatora=oib&identifikator=<OIB>&expand_relations=true`. Timeouts as in INGEST-1. Map the foreign JSON to a private record, then to our `CompanyInfo` (`name`, `seat.municipality`, `seat.county`, `legalForm`, `activityCode`). Foreign field names must not leak outside this package.
2. Convert `glavna_djelatnost` to NKD (`1072` becomes `10.72`).
3. `NkdAreaMapper`: NKD division (first two digits) to a set of `Area`. Port the table from `poc/opendata/nkd.py`.
4. A fixture implementation (reads the files from SETUP-1) so the demo works without keys. Choose by configuration: real client when both key variables are set.
5. Keys come only from environment variables. **Never log keys or OIBs.**

**Acceptance criteria:**
- [ ] Tests on saved Sudreg JSON: mapping of name, seat, legal form, NKD conversion (including a code with a leading zero).
- [ ] `NkdAreaMapper` test for at least 5 divisions.
- [ ] Without keys the app starts and the fixture client is used.
- [ ] Manual proof with the real key in the PR (response for one demo OIB, no secrets).

**Out of scope:** the REST endpoint (API-7), other registers.

---

# Package 4: API

**Size:** 4.8 d | **Needs from other packages:** package 1 (DATA-1, DATA-2); API-7 also needs INGEST-9 or the fixtures from SETUP-1

**Goal:** Build the REST API the web app uses: funding calls, regulations, sources, matching and company lookup by OIB, with OpenAPI docs.

**Steps, in this order:**
1. `API-1` API base: error handling, page DTO, OpenAPI docs (0.5 d)
2. `API-2` Funding calls: list with filters, and detail (1.0 d)
3. `API-3` Funding calls: filter counts (facets) (0.5 d)
4. `API-4` Regulations: list with filters, and detail (0.7 d)
5. `API-5` Sources and meta endpoints (0.4 d)
6. `API-6` Matching service and `POST /api/profiles/match` (1.2 d)
7. `API-7` `POST /api/profiles/from-oib` (0.5 d)

All routes are under `/api`. JSON is camelCase, dates are ISO-8601. List endpoints use our own page DTO (`content`, `totalElements`, `page`, `size`), not the Spring `Page` JSON. Each API step must put **real JSON examples** of its responses in the PR, because the web steps build TypeScript types from them.

**The package is done when** every step below is done and its acceptance criteria are checked.

## API-1: API base: error handling, page DTO, OpenAPI docs

**Size:** 0.5 d | **Depends on:** DATA-1 | **Blocks:** API-2, API-4, API-5

**Why:** One error shape and one page shape for the whole API means the frontend handles errors in one place. OpenAPI docs show the jury that the API is documented and can be used by other systems (Architecture criterion, 5 points).

**What to do:**
1. `common/ErrorResponse` record `{statusCode, message, error}`. The frontend (`apps/web/src/types/ErrorResponse.type.ts`) already expects this shape and shows `message`.
2. `common/GlobalExceptionHandler` (`@RestControllerAdvice`): `NotFoundException` gives 404, validation errors give 400 (message = first field and reason), `ExternalSourceException` gives 502, everything else gives 500 with a generic message. Stack traces go only to the log. **`message` is in Croatian.**
3. Exceptions `NotFoundException` and `ExternalSourceException` (with the source name).
4. `common/PageResponse<T>` record: `content`, `totalElements`, `page`, `size`. A helper that builds it from a Spring `Page`. Page size defaults to 20 and is capped at 100.
5. Configure springdoc (`springdoc-openapi-starter-webmvc-ui`, the dependency is added in DATA-1) so Swagger UI is at `/swagger-ui.html` and the spec at `/v3/api-docs`.

**Acceptance criteria:**
- [ ] A test controller or `@WebMvcTest` shows 404, 400 and 502 all return `{statusCode, message, error}` with a Croatian message.
- [ ] `size=1000` returns at most 100 items.
- [ ] Swagger UI opens in the browser.
- [ ] `mvnw.cmd -q compile` and tests pass.

**Out of scope:** real endpoints, authentication.

---

## API-2: Funding calls: list with filters, and detail

**Size:** 1 d | **Depends on:** API-1, DATA-2 | **Blocks:** API-3, API-6, WEB-5, WEB-6, AI-4, QUALITY-1

**Why:** This is the main endpoint of the product. The list and detail pages and the matching all use it.

**What to do:**
1. `GET /api/funding-calls`. Filter record `FundingCallFilter` with: `q` (search in title and provider, max 200 characters), `status` (default `OPEN`), `targetGroup`, `area`, `county`, `source`, `type`, `deadlineBefore`, `page` (from 0), `size`, `sort` (`deadline` or `publishedAt`, default `deadline` ascending with empty deadlines last). Use one `Specification` or one `@Query`, not many derived methods. Do not load the whole table into memory.
2. Calls with `hidden = true` or `duplicateOf != null` are never returned.
3. Response item `FundingCallSummary`: `id`, `title`, `provider`, `source` (code and display name), `type`, `status`, `deadline`, `deadlineNote`, `totalAmount`, `maxAmount`, `targetGroups`, `areas`, `counties`, `extractedByLlm`, `url`.
4. `GET /api/funding-calls/{id}` returns `FundingCallDetail`: everything above plus `minAmount`, `conditionsSummary`, `requiredDocuments`, `attachments`, `aiFields`, `fieldsReviewed`, `fetchedAt`, and a `source` object (code, name, URL). **Never return `sourceText`.** Unknown id gives 404 with a Croatian message.
5. Avoid N+1 queries on the list (use `@EntityGraph` or `join fetch`).

**Acceptance criteria:**
- [ ] `GET /api/funding-calls?status=OPEN&targetGroup=COMPANY` returns only open calls for companies, paged.
- [ ] Hidden and duplicate calls are not in the results.
- [ ] One list request does not run one query per row (check with `spring.jpa.show-sql`).
- [ ] `@WebMvcTest`: list happy path, detail happy path, detail 404, bad parameter 400.
- [ ] The PR has a `curl` example and the real JSON for list and detail.

**Out of scope:** filter counts (API-3), matching (API-6), eligibility (AI-4).

---

## API-3: Funding calls: filter counts (facets)

**Size:** 0.5 d | **Depends on:** API-2 | **Blocks:** WEB-5

**Why:** The filter UI should show counts like "Students (23)". It makes filtering feel fast and shows the user what exists.

**What to do:**
1. `GET /api/funding-calls/facets` accepts the same filters as the list. It returns counts per `status`, `targetGroup`, `area`, `county`, `source` and `type`: `{status: [{value, count}], ...}`.
2. Counts are computed in the database with `GROUP BY`, not in Java memory. Each facet ignores its own filter, so the user can still see other options.
3. Hidden and duplicate calls are not counted.

**Acceptance criteria:**
- [ ] With no filters, the counts sum to the total number of visible calls (test).
- [ ] With `targetGroup=STUDENT`, the `area` counts only include student calls, while the `targetGroup` counts still show all groups.
- [ ] `@WebMvcTest` happy path. Real JSON in the PR.

**Out of scope:** UI.

---

## API-4: Regulations: list with filters, and detail

**Size:** 0.7 d | **Depends on:** API-1, DATA-2 | **Blocks:** WEB-8

**Why:** Laws are an equal part of the product. This is the data for the laws pages.

**What to do:**
1. `GET /api/regulations` with filters `q` (title), `type`, `area`, `publishedFrom`, `publishedTo`, `page`, `size`. Default sort: `publishedAt` descending.
2. Item `RegulationSummary`: `id`, `title`, `type`, `gazetteNumber`, `publishedAt`, `areas`, `hasSummary`.
3. `GET /api/regulations/{id}` returns `RegulationDetail`: all fields, `summary` (can be `null`), `url`, `eliUrl`, `pdfUrl`, `enactedAt`, `eliMetadata`. Unknown id gives 404.
4. Add `GET /api/regulations/facets` with counts per `type` and `area` (same idea as API-3, one small method).

**Acceptance criteria:**
- [ ] `GET /api/regulations?type=LAW&publishedFrom=2026-01-01` returns only laws from 2026, newest first.
- [ ] `@WebMvcTest` for list, detail, 404.
- [ ] Real JSON in the PR.

**Out of scope:** AI summaries (AI-3), link between laws and funding calls (nice-to-have).

---

## API-5: Sources and meta endpoints

**Size:** 0.4 d | **Depends on:** API-1, DATA-2 | **Blocks:** WEB-9

**Why:** The jury scores "Use of open data" and "Architecture". A "Where does the data come from" page, backed by real numbers, answers both. The "data as of" date also protects us when some calls have closed by the time of scoring.

**What to do:**
1. `GET /api/sources`: for each source, `code`, `name`, `description` (what it covers), `url`, `method` (API, HTML, PDF), `recordCount`, `lastSuccessfulFetch` (from `SourceRunLog`, or the snapshot date), and `license` (text, "not verified" if unknown). Include Narodne novine and Sudreg as sources of type API. Static descriptions can live in a config file in `resources/`.
2. `GET /api/meta`: `{snapshotDate, lastIngestAt, counts: {openFundingCalls, regulations}}`.

**Acceptance criteria:**
- [ ] Each source in the `SourceCode` enum appears once.
- [ ] `snapshotDate` equals the date from the seed files (DATA-2).
- [ ] `@WebMvcTest` happy path. Real JSON in the PR.

**Out of scope:** UI (WEB-9).

---

## API-6: Matching service and `POST /api/profiles/match`

**Size:** 1.2 d | **Depends on:** API-2 | **Blocks:** WEB-4, API-7

**Why:** This is the core value: "calls that fit you, and why". The POC has the scoring in `poc/match.py`. We move it into a clean, tested service. Review of the POC found that the "why" is plain rules, not AI, so we present it honestly as a transparent explanation.

**What to do:**
1. `MatchingService`: a pure function over entities (no HTTP). Port from `poc/match.py` (`score()` and `_kljuc()` for removing duplicates by normalized title). Rules to keep:
   - Closed and expired calls are excluded.
   - If the call lists target groups, the profile type (or one of its extra types) must be in them.
   - A local call (specific counties) is excluded when the profile county does not match.
   - A call with no topic overlap (area, keywords, NKD) is excluded.
   - Bonus for open status, and for a deadline within 30 days.
2. Fix compared to the POC: a call with unknown status is no longer silently lowered. It is returned with `statusUnknown = true` so the UI can warn.
3. Endpoint `POST /api/profiles/match`. Body `ProfileRequest`: `{type: TargetGroup, additionalTypes?: TargetGroup[], areas: Area[], county?: string, keywords?: string[], activityCodes?: string[]}`. Response: list of `MatchResult` `{fundingCall: FundingCallSummary, score, reasons: string[], statusUnknown}`, sorted by `score`, max 20.
4. `reasons` are short Croatian sentences, for example "target group: companies", "area: energy", "deadline in 12 days".

**Acceptance criteria:**
- [ ] `MatchingServiceTest` with at least 6 cases: student from Rijeka interested in environment; citizen who wants an electric car; IT d.o.o. with NKD 62.01; expired deadline is excluded; local call from another county is excluded; duplicate titles are merged.
- [ ] `@WebMvcTest`: happy path and 400 for an empty or invalid profile.
- [ ] The PR has the real JSON for the three test profiles.

**Out of scope:** profile storage on the server, notifications, AI scoring.

---

## API-7: `POST /api/profiles/from-oib`

**Size:** 0.5 d | **Depends on:** API-6, and either INGEST-9 (real Sudreg client) or the fixtures from SETUP-1 | **Blocks:** WEB-3

**Why:** A company enters only its OIB and the app fills in the profile from the Court Register. This is the best "wow" moment of the demo and the proof that we connect open data sources (OIB, then Sudreg, then NKD, then matching).

**What to do:**
1. Define the interface `SudregClient` (`CompanyInfo findByOib(String oib)`), if it is not already created by INGEST-9. Two implementations: the real client (when keys exist) and a fixture client (reads the files from SETUP-1).
2. `POST /api/profiles/from-oib`, body `{oib}`. Validate the format (11 digits, OIB checksum). Response: `{companyName, seat: {municipality, county}, activityCode, profile: ProfileRequest, source: "sudreg"}`.
3. Map as in `poc/opendata/sudreg.py` (`profil_iz_oib`) and `poc/opendata/nkd.py` (NKD division to areas). The legal form decides the target group (d.o.o. gives `COMPANY`, association gives `ASSOCIATION`).
4. **Never log the OIB.** Unknown OIB gives 404. Sudreg down gives 502 `ExternalSourceException`.

**Acceptance criteria:**
- [ ] With the fixture client, each demo OIB from SETUP-1 returns a correct profile (test).
- [ ] Invalid OIB gives 400, unknown OIB gives 404, simulated Sudreg failure gives 502.
- [ ] No OIB appears in logs (check by reading the test log).
- [ ] The PR has the real JSON for each demo OIB.

**Out of scope:** the real Sudreg HTTP client (INGEST-9).

---

# Package 5: AI

**Size:** 3.2 d | **Needs from other packages:** package 1 (DATA-1); AI-2 needs INGEST-2, AI-3 needs INGEST-8, AI-4 needs API-2

**Goal:** Build the LLM client, the field extraction, the regulation summaries and the eligibility check. All AI results are saved in the database, so the demo works without a key.

**Steps, in this order:**
1. `AI-1` LLM client with budget limit, usage log and no-key fallback (0.7 d)
2. `AI-2` Extract fields and conditions summary from funding calls (1.0 d)
3. `AI-3` Summaries for regulations (0.5 d)
4. `AI-4` Eligibility check: backend (1.0 d)

**Decisions (26.10.):**
- **Claude API with a cache.** Results are computed in advance and saved in the database, so the demo and the video work without an API key and without internet. Only the eligibility check can call the model live, and it has a fallback.
- Models: `claude-haiku-5-5` for bulk extraction and summaries. Use `claude-sonnet-5-5` only if the accuracy measured in QUALITY-3 is not good enough. Check that the model id exists for the key the team uses before building on it.
- Cost: the POC measured about 0.0008 USD per call with Haiku (about 1.3 USD for all). We set a hard budget.
- Claude is listed in the documentation as an AI tool (Rules, section VIII). The rule "software is developed with open source" could be read as "runtime AI must be open source", so this is a question for the organizer (DELIVERY-5). The cache makes sure the product also works without the LLM.

**The package is done when** every step below is done and its acceptance criteria are checked.

## AI-1: LLM client with budget limit, usage log and no-key fallback

**Size:** 0.7 d | **Depends on:** DATA-1 | **Blocks:** AI-2, AI-3, AI-4

**Why:** All AI features share one client. A budget limit stops runaway cost, and a no-key fallback keeps the app running everywhere (also on a juror's laptop).

**What to do (package `integration/llm/`):**
1. Interface `LlmClient`: one method that takes a system prompt, a user text and a JSON schema, and returns the parsed JSON (structured output or tool use, not free text).
2. `AnthropicLlmClient`: the official Java SDK if one exists, otherwise `RestClient` against the Messages API (give the reason in the PR). Key from `ANTHROPIC_API_KEY`. Timeouts, and a retry on 429 and 5xx.
3. `NoopLlmClient` is used when there is no key. It throws `LlmUnavailableException`, and callers skip the AI step.
4. Every call writes an `LlmUsage` row (model, input and output tokens, cost in USD, purpose). The price table is in configuration (Haiku 0.10/0.50 USD, Sonnet 2.00/10.00 USD per million tokens, input/output, as in the POC config).
5. **Hard budget:** property `llm.budget-usd` (default 3). When the sum of `LlmUsage` reaches it, calls fail with a clear message.
6. Never log the key or full prompts with personal data.

**Acceptance criteria:**
- [ ] Unit tests with a fake HTTP server: valid JSON, invalid JSON, a null field, a 429 retry, budget reached.
- [ ] The app starts and works without `ANTHROPIC_API_KEY`.
- [ ] Tests never call the real API.

**Out of scope:** the actual features (AI-2 to AI-4).

---

## AI-2: Extract fields and conditions summary from funding calls

**Size:** 1 d | **Depends on:** AI-1, INGEST-2 | **Blocks:** AI-4, QUALITY-3

**Why:** Rules alone miss the deadline and amount in many records (deadline known for about 41 %), and sometimes get them wrong. The LLM reads the text and PDF and returns clean fields, plus a plain-language summary of who can apply and for what. This is the "innovation" part. Reference: `poc/extract/llm.py` (schema and prompt).

**What to do:**
1. `FundingCallExtractionService`: for each call without `extractedByLlm`, send `sourceText` (page text up to 15,000 characters, PDF text up to 25,000) and get JSON by the schema from `poc/extract/llm.py`: provider, type, publish date, deadline, total/min/max amount in EUR, target groups, areas (max 3), NKD codes, counties, `conditionsSummary` (1-3 Croatian sentences), `requiredDocuments` (max 10).
2. Keep the POC prompt rules: use only what is in the text, otherwise null; ignore amounts in kuna; `["sve"]` for whole-country calls.
3. **Merging:** the LLM value replaces the rule value only when the LLM returned a value (not null). Save the names of the fields set by AI in `aiFields` and set `extractedByLlm = true`.
4. Process **open and announced calls first**.
5. Admin route `POST /api/admin/ai/extract?limit=300` (same `X-Admin-Key` as INGEST-2), asynchronous, logs the cost per run.

**Acceptance criteria:**
- [ ] Unit tests with a fake LLM client: valid response, null fields, invalid JSON, merge rules.
- [ ] A batch on 20 open calls: the PR has a table (title, rule value, LLM value, correct yes/no by hand) and the total cost in USD.
- [ ] Control case: the FZOEU electric car call gives 9,000 EUR per vehicle and `deadline = null`.
- [ ] Without a key the app works and the step is skipped with a clear log line.
- [ ] Model, cost and result are written into `work/decisions.md`.

**Out of scope:** the eligibility check (AI-4), embeddings, chat.

---

## AI-3: Summaries for regulations

**Size:** 0.5 d | **Depends on:** AI-1, INGEST-8 | **Blocks:** WEB-8 (nicer detail page)

**Why:** A law title alone says little. A 2-3 sentence summary ("what it is about, who it affects, when it applies") is the quickest value for the user and a good demo moment.

**What to do:**
1. `RegulationSummaryService`: only for laws and the newest 60 regulations (not all 3,842). Take the text from the act page or PDF and ask for a 2-3 sentence Croatian summary. Also fill `areas` when rules did not find any.
2. The prompt forbids guessing: if the text does not contain the answer, return `null`.
3. Save into `Regulation.summary`. Admin route `POST /api/admin/ai/summarize-regulations?limit=60`.

**Acceptance criteria:**
- [ ] Summaries for the 10 newest laws, each with a link to Narodne novine. Check 3 of them by hand: no invented content.
- [ ] Unit tests with a fake LLM client (valid, null, invalid).
- [ ] Without a key the step is skipped.

**Out of scope:** linking laws to funding calls (nice-to-have).

---

## AI-4: Eligibility check: backend

**Size:** 1 d | **Depends on:** AI-2, API-2 | **Blocks:** WEB-7

**Why:** "Do I meet the conditions?" answers the user's real question and is the strongest AI feature in the video. It is a legal-style answer, so it must quote the source and show a disclaimer. It must also work when the model is not available.

**What to do:**
1. **Questions are made in advance.** During extraction (AI-2) the LLM also writes up to 5 short questions about the conditions of the call (`eligibilityQuestions`: `{id, text, type: YES_NO | NUMBER | CHOICE}`). They are saved with the call. Add this to the AI-2 schema, or run a separate batch `POST /api/admin/ai/generate-questions` here.
2. `GET /api/funding-calls/{id}/eligibility-questions` returns the questions (empty list if none).
3. `POST /api/funding-calls/{id}/eligibility-check`, body `{answers: {questionId: value}}`. Response: `{verdict: LIKELY_ELIGIBLE | LIKELY_NOT_ELIGIBLE | UNCLEAR, reasons: [{text, quote}], missingInfo: string[], disclaimer: string, sourceUrl: string}`.
4. One model call judges the answers against the conditions text. **Every reason must have a `quote`**, a literal passage from the call text. If the model cannot quote, the verdict is `UNCLEAR`. Never "you are sure to qualify".
5. **Fallback without a model:** simple rules over the answers and the profile (target group, county, deadline). When the model is not available, the answer is 200 with `verdict = UNCLEAR` and the rule-based reasons, not a 500.
6. **The disclaimer is required** in every response: "Informativna procjena na temelju teksta poziva, nije pravni savjet. Provjerite uvjete u službenoj dokumentaciji."
7. Limit input size and rate (for example 10 requests per minute per IP) so a demo cannot burn the budget.

**Acceptance criteria:**
- [ ] Unit tests with a fake LLM client: eligible, not eligible, model without quote gives `UNCLEAR`, model down gives the fallback.
- [ ] For 3 demo calls, 2 scenarios each (eligible and not eligible) give the expected verdict with a quote. Put them in the PR.
- [ ] The disclaimer and `sourceUrl` are in every response.
- [ ] Rate limit test: the 11th request in a minute gets 429 with a Croatian message.

**Out of scope:** UI (WEB-7), free-text chat over all calls.

---

# Package 6: Web: foundation, profile and recommendations

**Size:** 3.2 d | **Needs from other packages:** none for WEB-1 and WEB-2; WEB-3 needs API-7; WEB-4 needs API-6

**Goal:** Build the web foundation (layout, proxy, shared components, all routes), the profile form, the OIB lookup and the recommendations page. This is the main flow of the video.

**Steps, in this order:**
1. `WEB-1` Web foundation: proxy, layout, navigation, shared state components (1.0 d)
2. `WEB-2` Profile form with local storage (0.8 d)
3. `WEB-3` Company lookup by OIB in the profile form (0.4 d)
4. `WEB-4` Recommendations page (1.0 d)

**Rules for every web step (skill `react-web` is mandatory):**
- The current `apps/web` is an empty skeleton: `HomePage` with a title, `Router.tsx`, and the axios instance `api` in `src/api/base.ts`.
- Structure by the skill: `api/<domain>.api.ts`, `hooks/use<Name>.ts` with query keys, `types/<Name>.type.ts`, one folder per page in `pages/`, a barrel `index.ts` in every folder, imports through `@/`, route paths as constants in `routes/routes.ts`.
- **Types copy the real backend JSON exactly.** Until the API step is merged, use the JSON examples from its PR or issue.
- **Every screen with data has four states:** loading, error (message and a "Pokušaj ponovno" button that calls `refetch`), empty (say what to change), data. Screenshots of all four states go into the PR.
- UI text is Croatian, short and concrete. Format with `Intl` and `hr-HR` (dates like `23. 10. 2026.`, amounts in EUR). Highlight close deadlines ("još 3 dana").
- **Each data item shows its source** (name and link). Fields set by AI carry a visible "AI" label.
- Accessibility: semantic HTML, a `label` for every field, visible focus, good contrast. Must look good at 1280 px (the video) and work on a phone.
- No new UI libraries (no Tailwind, MUI and so on) without a team decision. Styles: CSS Modules and CSS variables in one global file.
- Visual style: one accent colour, a lot of white space, cards with a clear order (title, deadline, amount). The POC mini web (`git show origin/poc/natjecaji-scraper:poc/web/index.html`) is a reference for ideas, not code to copy.
- Check in the browser (`yarn dev` with the API running). `yarn lint` and `yarn build` must pass.

**The package is done when** every step below is done and its acceptance criteria are checked.

## WEB-1: Web foundation: proxy, layout, navigation, shared state components

**Size:** 1 d | **Depends on:** none | **Blocks:** all other WEB steps

**Why:** Every page needs the same shell and the same loading, error and empty components. Building them once keeps the other PRs small and the app consistent, which is what the UI/UX score looks at.

**What to do:**
1. Add the dev proxy in `vite.config.ts`: `server.proxy['/api'] = 'http://localhost:8080'`. Without it, API calls end up on Vite and return 404.
2. Global CSS: CSS variables for colours, spacing, font sizes, radius, and a small reset. One accent colour.
3. `layouts/AppLayout`: header with the app name and navigation (Početna, Moji natječaji, Svi natječaji, Zakoni, Izvori), main area, footer slot (filled in WEB-9). Responsive: the navigation collapses on small screens.
4. Routes as constants for all planned pages (`HOME`, `RECOMMENDATIONS`, `FUNDING_CALLS`, `FUNDING_CALL_DETAIL`, `REGULATIONS`, `REGULATION_DETAIL`, `SOURCES`). Pages that do not exist yet can show a simple placeholder.
5. Shared components in `components/`: `LoadingState` (skeleton or message), `ErrorState` (message and retry button), `EmptyState` (message and hint), `PageHeader`, `Card`, `Badge` (used for the source and the "AI" label), `ExternalLink` (opens in a new tab, `rel="noopener noreferrer"`).
6. Helpers in `utils/`: `formatDate`, `formatAmount`, `formatDeadline` (returns for example "još 3 dana" or "isteklo"). Unit-test them if the project has a test setup, otherwise keep them pure and simple.
7. Shared types `Page.type.ts` (page response) and `ErrorResponse` stay as they are.

**Acceptance criteria:**
- [ ] `yarn dev` shows the layout, navigation works between placeholder pages.
- [ ] `/api/...` calls go through the proxy (check with one real call).
- [ ] The shared state components are used on at least one placeholder page, with screenshots.
- [ ] `yarn lint` and `yarn build` pass.

**Out of scope:** real data pages, dark theme.

---

## WEB-2: Profile form with local storage

**Size:** 0.8 d | **Depends on:** WEB-1 | **Blocks:** WEB-3, WEB-4

**Why:** The profile is the entry point to the whole product. There are no accounts (decision of 26.10.), so the profile lives in the browser. A short form that takes under a minute is the best first impression.

**What to do:**
1. Page `HomePage`: a title and one sentence of value ("Svi natječaji i novi zakoni na jednom mjestu"), then the profile form.
2. Fields: type (Građanin, Student, Obrt, Tvrtka, Udruga, Poljoprivrednik, Jedinica lokalne samouprave), county (select), interests (multi-select of areas with Croatian names), optional keywords. Map the values to the API enums (`TargetGroup`, `Area`).
3. `hooks/useProfile`: reads and writes the profile in `localStorage`, validates it, survives a page refresh. Button "Prikaži moje natječaje" saves and goes to the recommendations route. If a profile already exists, the form is prefilled and the page offers "Promijeni profil".
4. Form validation with clear messages (type and at least one interest are required).
5. Types `Profile.type.ts` that match `ProfileRequest` from API-6.

**Acceptance criteria:**
- [ ] The profile survives a refresh and can be edited or cleared.
- [ ] Every field has a `label`, the form works with the keyboard only.
- [ ] Screenshots: empty form, filled form, validation error.
- [ ] `yarn lint` and `yarn build` pass.

**Out of scope:** the OIB lookup (WEB-3), accounts, server-side profile.

---

## WEB-3: Company lookup by OIB in the profile form

**Size:** 0.4 d | **Depends on:** WEB-2, API-7 | **Blocks:** DELIVERY-2

**Why:** The demo moment: type an OIB and the form fills itself from the Court Register. It shows data connection, and it saves the user's time (challenge C).

**What to do:**
1. In the profile form add an input "Ili upiši OIB tvrtke" and a button "Dohvati podatke". It calls `POST /api/profiles/from-oib` through a mutation hook.
2. Show the result in a small box: "Pronašli smo: <name>, <city>, djelatnost <NKD>" with the label "Izvor: Sudski registar". Then prefill the form (type, county, interests) and let the user change it.
3. States: loading on the button, a clear message for "OIB nije valjan" (400), "Tvrtka nije pronađena" (404) and "Sudski registar trenutno nije dostupan" (502), each with the option to continue by filling in the form by hand.
4. The OIB is not saved in `localStorage` (only the resulting profile) and is never logged.

**Acceptance criteria:**
- [ ] A demo OIB from SETUP-1 prefills the form (works with real Sudreg or with the fixture).
- [ ] All three error cases show the right Croatian message (test by changing the input or stopping the API).
- [ ] Screenshots of success and error.
- [ ] `yarn lint` and `yarn build` pass.

**Out of scope:** the backend (API-7).

---

## WEB-4: Recommendations page

**Size:** 1 d | **Depends on:** WEB-2, API-6 | **Blocks:** DELIVERY-2

**Why:** This is the "wow" page of the product: the calls that fit me, and why. It is the main screen of the video.

**What to do:**
1. Route `/preporuke`. If there is no saved profile, redirect to the home page with a short message.
2. Call `POST /api/profiles/match` through a hook (`useRecommendations(profile)`).
3. Card for each result: title, provider, deadline (highlight when close), amount, source badge, "AI" label if the data comes from AI, a block **"Zašto ti odgovara"** with the `reasons` list, and a warning when `statusUnknown` is true ("Status natječaja nije potvrđen, provjeri na izvoru"). Clicking the card opens the detail page. Build the card as `components/FundingCallCard` so WEB-5 can reuse it.
4. A short summary at the top: "Pronašli smo N natječaja za <profile type>".
5. Empty state: "Nema natječaja za ovaj profil" with a hint to widen the interests or county, and a link back to the profile.

**Acceptance criteria:**
- [ ] The three test profiles (student from Rijeka, citizen with an e-car, IT d.o.o.) give sensible lists.
- [ ] Screenshots of all four states.
- [ ] The source link is visible on every card.
- [ ] `yarn lint` and `yarn build` pass.

**Out of scope:** saving favourites, notifications.

---

# Package 7: Web: lists, details, laws and sources

**Size:** 3.9 d | **Needs from other packages:** package 6 (WEB-1, WEB-4); API-2 to API-5; AI-4 for WEB-7

**Goal:** Build the browse pages: funding call list and detail, the eligibility panel, regulations, and the sources page with the data date.

**Steps, in this order:**
1. `WEB-5` Funding call list with filters in the URL (1.0 d)
2. `WEB-6` Funding call detail page (0.7 d)
3. `WEB-7` Eligibility check panel (0.7 d)
4. `WEB-8` Regulations list and detail pages (1.0 d)
5. `WEB-9` Sources page and "data as of" footer (0.5 d)

**Note:** The web rules from package 6 apply to every step in this package too.

**The package is done when** every step below is done and its acceptance criteria are checked.

## WEB-5: Funding call list with filters in the URL

**Size:** 1 d | **Depends on:** WEB-1, WEB-4, API-2, API-3 | **Blocks:** none

**Why:** Users who do not want a profile can still browse and filter all calls. The filters in the URL make links shareable and the back button work, which shows careful UX thinking.

**What to do:**
1. Route `/natjecaji`. Search input (`q`), filters for status, target group, area, county, source and type, with counts from the facets endpoint (for example "Studenti (23)"), a sort selector, and pagination.
2. **Filter state lives in the URL query string** (wouter `useSearchParams`). Opening a copied link shows the same list. A "Poništi filtre" button clears them.
3. Use `placeholderData: keepPreviousData` so the list does not flicker while filters change. Debounce the search input (about 300 ms).
4. Reuse `components/FundingCallCard` from WEB-4 (package 6). Start the list page after WEB-4 is merged, so the card is not built twice.
5. On small screens the filters collapse behind a "Filtri" button.

**Acceptance criteria:**
- [ ] Changing any filter updates the URL and the list. Pasting the URL in a new tab gives the same view.
- [ ] Screenshots of all four states and of a filtered view.
- [ ] Keyboard-only use works.
- [ ] `yarn lint` and `yarn build` pass.

**Out of scope:** saved searches, sorting by relevance.

---

## WEB-6: Funding call detail page

**Size:** 0.7 d | **Depends on:** WEB-1, API-2 | **Blocks:** WEB-7

**Why:** After a card, the user wants all the facts and the link to the official source. Trust (visible source, "AI" labels) is built here.

**What to do:**
1. Route `/natjecaji/:id`. Show: title, provider, status badge, deadline (or the deadline note), amounts (total, minimum, maximum), target groups and areas, counties.
2. Block "Sažetak uvjeta" with the `conditionsSummary` and an "AI" label when it is AI-written. Block "Potrebni dokumenti" (list). Block "Prilozi" with links (open in a new tab).
3. A clear **"Izvor" block**: source name, a link to the original page, and the date the data was fetched ("Podaci dohvaćeni: <date>").
4. A warning when the deadline has passed or the status is unknown.
5. A place for the eligibility panel (WEB-7): an empty section that WEB-7 fills. Keep it hidden until WEB-7 is merged.
6. A back link that returns to the previous list with the same filters.

**Acceptance criteria:**
- [ ] The page works for a call with all fields and for a call with many empty fields (no "null" or "undefined" text).
- [ ] 404 shows a clear message and a link to the list.
- [ ] Screenshots of all four states.
- [ ] `yarn lint` and `yarn build` pass.

**Out of scope:** the eligibility panel (WEB-7), sharing buttons.

---

## WEB-7: Eligibility check panel

**Size:** 0.7 d | **Depends on:** WEB-6, AI-4 | **Blocks:** DELIVERY-2

**Why:** "Ispunjavam li uvjete?" is the main AI feature in the video. The UI must make it simple and honest: short questions, a clear result, a quote from the source, and a disclaimer.

**What to do:**
1. On the detail page add the panel "Ispunjavam li uvjete?". Load the questions from `GET /api/funding-calls/{id}/eligibility-questions`. If there are none, hide the panel.
2. Render a short form by question type (yes/no, number, choice). Button "Provjeri". Send the answers to the check endpoint with a mutation.
3. Show the result: a clear verdict (likely eligible, likely not eligible, unclear) in words and colour (do not use only colour), the list of reasons each with its **quote** from the call text, "Što još trebaš provjeriti" from `missingInfo`, and the **disclaimer** text from the response, always visible.
4. Show a link to the official call (`sourceUrl`) next to the result.
5. States: loading on the button, an error with retry, and a friendly message for the rate limit (429).

**Acceptance criteria:**
- [ ] Works for the demo calls prepared in AI-4, both scenarios.
- [ ] The disclaimer and source link are always shown with a result.
- [ ] Screenshots: questions, loading, each verdict, error.
- [ ] `yarn lint` and `yarn build` pass.

**Out of scope:** free-text chat, saving answers.

---

## WEB-8: Regulations list and detail pages

**Size:** 1 d | **Depends on:** WEB-1, API-4 | **Blocks:** DELIVERY-2

**Why:** Laws are an equal part of the product (team decision). Users should be able to find new laws by type, period and area.

**What to do:**
1. Route `/zakoni`: list with search, filters (type, publish period, area) with counts from the facets endpoint, pagination, filters in the URL (same approach as WEB-5). Card: title, NN number, date, type, area badges, and a short note if a summary exists.
2. Route `/zakoni/:id`: metadata (NN number, publish date, enactment date, type), the AI summary if it exists (with an "AI" label and a note that it is not legal advice), and links to Narodne novine, ELI and the PDF.
3. Highlight new laws (published in the last 30 days).
4. Four states on both pages. 404 for an unknown id.

**Acceptance criteria:**
- [ ] The list filters by type and period, and the URL keeps the filters.
- [ ] A law without a summary and a law with one both render well.
- [ ] Screenshots of all four states on the list, and of the detail page.
- [ ] `yarn lint` and `yarn build` pass.

**Out of scope:** the link between laws and funding calls (nice-to-have), law text on our page.

---

## WEB-9: Sources page and "data as of" footer

**Size:** 0.5 d | **Depends on:** WEB-1, API-5 | **Blocks:** none

**Why:** It answers "Use of open data" and "Architecture" directly, and it protects us when some calls close before scoring: the footer says the data is from a given day.

**What to do:**
1. Route `/izvori`: a short text (what is an API, what do we read from pages, what is a snapshot), and a table or cards from `GET /api/sources`: name, what it covers, method, number of records, last fetch, license note, link.
2. Footer in `AppLayout`: "Podaci stanje na dan: <snapshotDate>" from `GET /api/meta`, and the note "Informativno, nije pravni savjet".
3. Four states for the page. The footer must not break the page if `/api/meta` fails (show nothing or a quiet fallback).

**Acceptance criteria:**
- [ ] Every source from the API is listed with a working link.
- [ ] The date in the footer equals `snapshotDate`.
- [ ] Screenshots of the page and the footer.
- [ ] `yarn lint` and `yarn build` pass.

**Out of scope:** admin functions.

---

# Package 8: Quality and delivery

**Size:** 6.2 d | **Needs from other packages:** API-2, the INGEST steps, AI-2 and the web packages; DELIVERY-1 can start now

**Goal:** Check the data we show, then make the video, the documentation and the submission package.

**Steps, in this order:**
1. `QUALITY-1` Review tool: CSV export/import, hide noise, mark duplicates (0.8 d)
2. `QUALITY-2` Manual review of the demo set (50+ open calls) (1.0 d)
3. `QUALITY-3` Measure extraction accuracy on 15 samples (0.5 d)
4. `DELIVERY-1` Video script (0.5 d)
5. `DELIVERY-2` Record and edit the video (1.5 d)
6. `DELIVERY-3` Documentation part 1: problem, architecture, data source list (0.7 d)
7. `DELIVERY-4` Documentation part 2: AI tools, data quality, standard proposal, how to run (0.7 d)
8. `DELIVERY-5` Submission package: code delivery, email, questions to the organizer (0.5 d)

**Note:** This package is shared. The team can create it as one issue with a checklist, or split it into a QUALITY issue and a DELIVERY issue. QUALITY-1 to QUALITY-3 are one block of work, DELIVERY-1 to DELIVERY-5 are another.

**Why the data checks exist:** The POC review showed that the data is weaker than the report sounds: deadline known for 33-41 % of records, status for 55-69 %, target groups too wide, duplicates and noise in the lists, and LLM accuracy never checked by hand. The jury sees only a few calls in the video. One wrong deadline or amount there costs trust and points in UI/UX and video. So we check by hand **the set we show**, not the whole database.

**The package is done when** every step below is done and its acceptance criteria are checked.

## QUALITY-1: Review tool: CSV export/import, hide noise, mark duplicates

**Size:** 0.8 d | **Depends on:** API-2 | **Blocks:** QUALITY-2

**Why:** Fixing 50+ records by hand needs a simple tool. A CSV in a spreadsheet is the quickest for a team of four.

**What to do:**
1. `GET /api/admin/review/export?status=OPEN` returns a CSV with `id`, `source`, `title`, `url`, the current values (deadline, total and max amount, target groups, areas, counties) and empty columns `correct_deadline`, `correct_amount`, `correct_target_groups`, `correct_areas`, `hide` (yes/no), `duplicate_of_id`, `notes`.
2. `POST /api/admin/review/import` reads the edited CSV, applies the corrections and sets `fieldsReviewed = true` for each processed row. `hide = yes` sets `hidden = true` with the note as `hiddenReason`. `duplicate_of_id` sets `duplicateOf`. Return a report: how many rows were changed, skipped, or had errors (with row numbers).
3. Both routes are protected by `X-Admin-Key`.
4. **Automatic duplicate finder:** `POST /api/admin/review/find-duplicates` marks likely duplicates between sources with the rule from `poc/match.py` (`_kljuc`: normalized title, first 80 characters) and returns the groups for human confirmation. It does not change data by itself.

**Acceptance criteria:**
- [ ] Round trip test: export, change two cells, import, and the values are changed and `fieldsReviewed = true`.
- [ ] A bad row (wrong date format) is reported with its row number and does not stop the import.
- [ ] The list API (API-2) no longer returns hidden and duplicate calls.
- [ ] The routes return 401 without the key.

**Out of scope:** a UI for review.

---

## QUALITY-2: Manual review of the demo set (50+ open calls)

**Size:** 1 d (manual work, can be split between people) | **Depends on:** QUALITY-1, INGEST-3 to INGEST-7 | **Blocks:** DELIVERY-2

**Why:** The video and the screenshots use these calls. Everything shown must be true.

**What to do:**
1. Choose **at least 50 open calls from all 6 sources**. Put first those that appear for the three demo profiles (student from Rijeka and environment; citizen with an e-car; IT d.o.o. by the OIB from SETUP-1).
2. For each, check on the official page: is it still open, deadline, amount, target groups, areas, county. Fix it in the CSV (QUALITY-1) and import.
3. Hide records that are not calls (news about awarded grants, workshops, ads) and mark duplicates.
4. For the three demo profiles check that the **top 5 recommendations are reviewed calls, without duplicates, and each "why it fits" is true**.
5. Make a short list of **known data limits** (what we cover, what we do not, how many fields are empty) for the documentation.

**Notes:**
- Do not clean closed calls. Some open calls will expire before scoring: do not try to hide this. The UI says "data as of <date>" (WEB-9).
- Never change data to look better than the source. A field the source does not have stays empty.

**Acceptance criteria:**
- [ ] At least 50 open calls have `fieldsReviewed = true`, and all calls in the three demo profiles' top 5 are among them.
- [ ] The list of known limits is in a comment on the issue.
- [ ] Duplicates and noise are not visible in `/api/funding-calls`.

**Out of scope:** cleaning the whole database.

---

## QUALITY-3: Measure extraction accuracy on 15 samples

**Size:** 0.5 d | **Depends on:** AI-2, QUALITY-2 | **Blocks:** DELIVERY-4

**Why:** The POC compared two models with each other (they agree), but never compared them to the truth. The jury trusts a measured number more than a claim. Reference: `poc/eval/make_sample.py`, `poc/eval/usporedi.py`, `poc/eval/uzorak.csv`.

**What to do:**
1. Take 15 random open calls. For each, write by hand the correct deadline, total amount, max amount, target groups and areas.
2. Compare with the rules and with the LLM. Compute accuracy per field.
3. Target: **deadline and amount at least 85 %**. If the LLM is under it, test Sonnet on the same 15 and decide whether the extra cost is worth it.
4. Write the table and the decision into `work/decisions.md`. Include what does not work well.

**Acceptance criteria:**
- [ ] A table of accuracy per field for rules vs LLM in the issue and in `work/decisions.md`.
- [ ] A decision on the model (Haiku or Sonnet) with the reason.

**Out of scope:** measuring the whole database.

---

## DELIVERY-1: Video script

**Size:** 0.5 d | **Depends on:** none (**start now**, do not wait for code) | **Blocks:** DELIVERY-2

**Why:** The video is worth 10 points and is the only way the jury sees everything together: scoring uses only what we send, with no live presentation. The rules ask the video (max 3 minutes) to cover: problem, target users, data sources, functions, technical architecture, user value, future development. A written script also shows what the product must be able to do, so it guides the other steps.

**What to do:** write `docs/video-script.md`, second by second, with the exact screen and click for each part. Suggested structure:
- 0:00-0:25 **Problem:** funding calls and laws are spread over dozens of sites, often in PDF. A concrete example ("A company with 5 employees looks for a subsidy: how many pages must it open?").
- 0:25-1:45 **Demo:** enter a company OIB, the app reads activity and seat from the Court Register, shows recommendations with "why it fits"; open a call, show the AI summary and the eligibility check with a quote; quickly show laws with an AI summary.
- 1:45-2:20 **Architecture and data:** one picture: sources (Sudreg API, Narodne novine ELI, six bodies, data.gov.hr where used) to Java ingest to Postgres to API to React, and Claude with a cache. Say that the source is visible next to every data item.
- 2:20-2:50 **Future and standard:** the proposed standard "Open data about calls" (one JSON format that bodies could publish on data.gov.hr), more sources, notifications, link between laws and calls.
- 2:50-3:00 closing line and team name.

**Acceptance criteria:**
- [ ] The script has time marks and covers all seven points the rules ask for.
- [ ] The whole team has read it (by 29.10.).
- [ ] A list of what the demo needs ("demo needs": OIB, 3 calls, 2 eligibility scenarios, 1 law with a summary) that feeds QUALITY-2 and AI-4.

**Out of scope:** recording.

---

## DELIVERY-2: Record and edit the video

**Size:** 1.5 d | **Depends on:** WEB-4, WEB-6, WEB-7, WEB-8, QUALITY-2, DELIVERY-1 | **Blocks:** DELIVERY-5

**Why:** No video means no score.

**What to do:**
1. Prepare the demo data (only reviewed calls, the real OIB from SETUP-1). Prefer an app that runs without internet (cache).
2. Record the screen at 1920x1080 (or 1280x720) with a clear voice in Croatian (or subtitles). At least two takes.
3. Edit: cuts, zoom on important parts, subtitles for key numbers. **Length at most 3:00.** Export MP4 (H.264).
4. Consent: the video is published only with the consent given in the application form. If a person is visible or heard, check that consent was given.
5. Save the file in an approved place (see DELIVERY-5).

**Acceptance criteria:**
- [ ] Length 3:00 or less, covers the seven required points.
- [ ] Every call shown is reviewed (QUALITY-2) and is not expired on the recording day.
- [ ] Sound is clear and the text is readable on a laptop screen.

**Out of scope:** animation, English version.

---

## DELIVERY-3: Documentation part 1: problem, architecture, data source list

**Size:** 0.7 d | **Depends on:** write in parallel with development, finish after the INGEST steps | **Blocks:** DELIVERY-5

**Why:** The documentation is required and is scored under "Architecture" (5 points) and "Use of open data" (5 points). **The rules explicitly require a list of all used datasets with publishers and links.** Without it the submission is incomplete.

**What to do:** write the first half of `docs/DOKUMENTACIJA.md` (Croatian):
1. **Problem and users** (citizens, students, craftsmen, companies, associations, farmers) and the benefit: saved time and cost of finding information (challenge C).
2. **Architecture:** a diagram (Mermaid or an image in the repo) and text: sources, Java ingest (`PoliteHttpClient`, parsers, rules), Postgres, Spring API (OpenAPI), React, AI layer (Claude with a cache), Sudreg and Narodne novine as live APIs. Why this stack.
3. **Data source list (REQUIRED).** A table: name, publisher, URL, how we get it (API / HTML / PDF), license or reuse terms, robots.txt note, what we use. Include: Associations Office, FZOEU, fondovieu (NPOO), HZZ, APPRRR, MZOM, Narodne novine (ELI), Sudski registar, NKD, and **every data.gov.hr dataset we really use** (CKAN). **For each source check and write the reuse terms. Do not assume that content from state websites has an open license. If not checked, write "not verified".**
4. **Connecting data:** how the data joins (OIB to Sudreg to NKD to areas to calls; NN ELI to regulations) and what new value this gives.
5. **Prerequisites for production:** technical (scheduler, monitoring), organizational (cooperation with the bodies), legal (reuse, GDPR for OIB), and which interoperability standards are used.

**Acceptance criteria:**
- [ ] The source table has a publisher and a link for **every** source the code uses (check against the `SourceCode` enum and the integrations).
- [ ] Each source has a license note or "not verified".
- [ ] The diagram renders.

**Out of scope:** part 2 (DELIVERY-4).

---

## DELIVERY-4: Documentation part 2: AI tools, data quality, standard proposal, how to run

**Size:** 0.7 d | **Depends on:** AI-2, QUALITY-3, DELIVERY-3 | **Blocks:** DELIVERY-5

**Why:** The rules require that **all AI tools are listed** and that the work is the team's own. The data quality section shows honesty about limits, and the standard proposal is our best answer to "why not an API from the bodies": it makes the point that the Ministry (organizer and owner of data.gov.hr) can use.

**What to do:** the second half of `docs/DOKUMENTACIJA.md`:
1. **AI and tools:** which models we use and for what, how the source and disclaimer are ensured, the cost. **A list of all AI tools used in development (Claude Code) and of libraries with licenses.** A statement of originality.
2. **Data quality and limits** (from QUALITY-2 and QUALITY-3): accuracy per field, what we do not cover, why we narrowed scope.
3. **Proposed standard "Open data about calls":** one page and a JSON example (DCAT-AP at dataset level, a JSON record with provider OIB, controlled vocabularies, NKD 2025, NUTS codes, a `results` field to link to who received the money). Start from `poc/docs/zakljucak.md`, section "Prijedlog standarda". The argument: not "we scraped sites", but "this is how hard it is and how one standard would fix it".
4. **How to run:** step by step for a new person: `docker compose up -d`, API, web, environment variables, seed data.
5. **Future development, team, mentor, license** (choose an OSS license such as MIT or Apache-2.0 once the public repository question is solved).

**Acceptance criteria:**
- [ ] The AI tools list includes Claude Code and the Claude API.
- [ ] Someone who did not work on the project follows "How to run" on a clean checkout and gets a working app.
- [ ] The standard proposal has a JSON example that is valid JSON.

**Out of scope:** an English version.

---

## DELIVERY-5: Submission package: code delivery, email, questions to the organizer

**Size:** 0.5 d | **Depends on:** everything | **Blocks:** nothing (the deadline is 9.11.2026 at 24:00; aim to send on 8.11.)

**Why:** The rules say late or incomplete submissions (no video) are not scored. The package must be complete and safe.

**What to do:**
1. **Questions to the organizer** (open.data@mpudt.hr), send this week: does registration oblige us to submit, and can the challenge be changed; under which criterion is open source publication scored; is a runtime AI via an API acceptable under "software of open source". Record the answers in `work/decisions.md`.
2. **Code delivery.** Public publication is **still an open question** because of the organization's security policy (see `work/decisions.md`). Until it is decided, nothing is published. Prepare two variants: (a) a public repository if approved, (b) a code archive (zip, without secrets, `node_modules`, `target`, `.idea`) in an **approved place** with a link that stays valid until the results are published (17.11. at least). Do not use public file sharing.
3. **Check the repository:** `git ls-files` has no secrets, no `.idea/`, no `.env`, and the `README.md` run instructions work.
4. **Email** to open.data@mpudt.hr with: code or link, documentation, demo material (screenshots), the video or its link.
5. Keep the sent email and confirm that it was received.

**Acceptance criteria:**
- [ ] The email is sent before 9.11.2026 at 24:00 (target 8.11.), with all four parts.
- [ ] The video and code links stay valid until the results are published.
- [ ] A secrets and junk check of the repository is done (named person signs off in the issue).

**Out of scope:** the conference on 23.11.

---

# Nice-to-have (open an issue only when MVP is done)
- **NICE-1 Scheduler and a real diff between two fetches** (`@Scheduled`, diff by `rawTextHash`), then real notifications by email (Mailpit or SMTP). The POC `notify_dry_run.py` is only a simulation by `datum_objave`, not by a real diff.
- **NICE-2 Link between laws and funding calls:** calls cite an NN number as the legal basis ("Narodne novine, broj 98/26"). Join it with the regulation ELI. The best argument for "Connecting data", but only after AI-2.
- **NICE-3 Standard as a JSON export:** `GET /api/funding-calls/export` in the proposed standard format (if it does not fit into DELIVERY-4).
- **NICE-4 More sources:** eufondovi.gov.hr (EU programme 2021-2027), Registar udruga by OIB, the Public bodies register as a provider reference list, NKD 2025 through data.gov.hr (CKAN).
- **NICE-5** HAMAG-BICRO, Osijek, Rijeka, Zagreb, AMPEU: dropped. Return only if everything else is done.

# Work order (critical path)
1. **Days 1-2 (26-27.10.):** package 1 (merge DATA-1 first), INGEST-1, WEB-1 and DELIVERY-1. API-1 starts as soon as DATA-1 is merged.
2. **Days 3-5 (28-30.10.):** INGEST-2 to INGEST-4, API-1 to API-5, AI-1, WEB-2.
3. **Days 6-8 (31.10.-2.11.):** package 3, API-6 and API-7, AI-2, WEB-3 and WEB-4. **By 1.11. a skeleton must stand: seed data, API and a list in the UI.** If not, cut scope.
4. **Days 9-11 (3-5.11.):** AI-3, AI-4, QUALITY-1 to QUALITY-3, package 7, DELIVERY-3 and DELIVERY-4. QUALITY-2 is manual work and is best split between everybody.
5. **6.11. the prototype is frozen.** 6-8.11.: DELIVERY-2 (video), DELIVERY-5 (package).
6. **9.11. by 24:00** the email must be sent. Aim for 8.11.

# Package dependencies
- **Package 1** blocks everything. DATA-1 is the gate for packages 2, 4 and 5. SETUP-1 blocks INGEST-9, API-7 and WEB-3.
- **INGEST-2** (package 2) blocks package 3 and AI-2.
- **API-2** blocks API-3, API-6, AI-4 and the list and detail pages. **API-6** blocks API-7 and WEB-4.
- **AI-1** blocks AI-2, AI-3 and AI-4. **AI-4** blocks WEB-7.
- **WEB-1 and WEB-4** (package 6) block package 7 (WEB-4 provides `FundingCallCard`).
- **Packages 6 and 7 and QUALITY-2** block DELIVERY-2 (the video).
- **DELIVERY-3 and DELIVERY-4** are written in parallel and finished after the INGEST, AI and QUALITY steps.
