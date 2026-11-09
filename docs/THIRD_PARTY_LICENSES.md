# Licence tuđih komponenti

Popis ovisnosti, fontova, ikona i servisa koje OtvoreniRadar koristi. Stanje 8. 11. 2026. Sve su licence otvorenog koda i kompatibilne s licencom projekta. Nema GPL/AGPL ovisnosti bez iznimke, nema nekomercijalnih ni nepoznatih licenci.

Kako ponovno generirati popis:

```bash
cd apps/web && npx license-checker --production --summary
cd apps/api && ./mvnw org.codehaus.mojo:license-maven-plugin:2.4.0:add-third-party -Dlicense.includedScopes=compile,runtime   # rezultat: target/generated-sources/license/THIRD-PARTY.txt
```

## Web (`apps/web`), ovisnosti u produkcijskom buildu

| Paket | Verzija | Licenca | Poveznica |
|---|---|---|---|
| react, react-dom | 19.3.1 | MIT | https://github.com/facebook/react |
| @tanstack/react-query | 5.104.5 | MIT | https://github.com/TanStack/query |
| axios | 1.20.0 | MIT | https://github.com/axios/axios |
| react-error-boundary | 6.1.6 | MIT | https://github.com/bvaughn/react-error-boundary |
| wouter | 3.13.0 | Unlicense (javno dobro) | https://github.com/molefrog/wouter |

Tranzitivne produkcijske ovisnosti (32 paketa, npr. `form-data`, `follow-redirects`, `scheduler`, `regexparam`) su sve MIT.

Alati za razvoj i build (ne isporučuju se korisniku): Vite, TypeScript, ESLint, typescript-eslint i ostalo, ukupno oko 190 paketa. Licence: MIT 153, Apache-2.0 15, ISC 10, BSD-2/3-Clause 8, MPL-2.0 2 (`lightningcss`), CC-BY-4.0 1 (`caniuse-lite`, podaci o preglednicima), BlueOak-1.0.0 1 (`minimatch`), Unlicense 1. MPL-2.0 je slabi copyleft na razini datoteke i odnosi se samo na izmjene samog `lightningcss`a, koji ne mijenjamo.

## API (`apps/api`), izravne ovisnosti

| Paket | Verzija | Licenca | Poveznica |
|---|---|---|---|
| Spring Boot (starter-webmvc, data-jpa, validation, devtools) | 4.1.2 | Apache-2.0 | https://spring.io/projects/spring-boot |
| Hibernate ORM / Validator | 7.4.13 / 9.1.5 | Apache-2.0 | https://hibernate.org |
| PostgreSQL JDBC Driver | 42.7.14 | BSD-2-Clause | https://jdbc.postgresql.org |
| jsoup | 1.23.2 | MIT | https://jsoup.org |
| Apache PDFBox | 3.0.8 | Apache-2.0 | https://pdfbox.apache.org |
| springdoc-openapi (+ Swagger UI 5.34.0) | 3.2.0 | Apache-2.0 | https://springdoc.org |
| anthropic-java (Claude API SDK) | 3.3.0 | Apache-2.0 | https://github.com/anthropics/anthropic-sdk-java |
| Project Lombok (samo pri prevođenju) | 1.18.48 | MIT | https://projectlombok.org |

Tranzitivne ovisnosti (121 ukupno): uglavnom Apache-2.0 (Spring, Jackson, Tomcat, OkHttp, Kotlin stdlib, Micrometer, OpenTelemetry), MIT (SLF4J, Checker Qual, standardwebhooks), BSD-3-Clause (ANTLR). Jakarta API-ji su pod EPL-2.0 / Eclipse Distribution License 1.0 / GPL-2.0 with Classpath Exception (dvojne licence, biramo EPL-2.0 ili EDL). Logback je dvojno EPL-2.0 / LGPL-2.1 (biramo EPL-2.0). Ništa od toga ne nameće copyleft na naš kod.

## Baza i infrastruktura

| Komponenta | Licenca | Poveznica |
|---|---|---|
| PostgreSQL (Docker slika `postgres`) | PostgreSQL License | https://www.postgresql.org/about/licence/ |
| Maven Wrapper | Apache-2.0 | https://maven.apache.org/wrapper/ |

## Fontovi, ikone, grafika

| Resurs | Licenca | Napomena |
|---|---|---|
| IBM Plex Sans | SIL Open Font License 1.1 | učitava se s Google Fonts |
| Source Serif 4 | SIL Open Font License 1.1 | učitava se s Google Fonts |
| Lucide ikone (putanje kopirane u `apps/web/src/components/Icon/Icon.tsx`) | ISC, © Lucide Contributors | atribucija u komentaru datoteke i ovdje |
| Logo i favicon (radar) | vlastiti rad tima | |

Kartografske podloge (OpenStreetMap i sl.) ne koristimo.

## Vanjski servisi

| Servis | Svrha | Uvjeti |
|---|---|---|
| Claude API (Anthropic) | izvlačenje polja, sažeci propisa, pitanja i provjera uvjeta | https://www.anthropic.com/legal/commercial-terms. Aplikacija radi i bez ključa (rezultati su spremljeni u seed). |
| Google Fonts | posluživanje fontova | https://developers.google.com/fonts/terms |

## Kopirani kod

Osim Lucide putanja (gore) u kodu nema kopiranog tuđeg koda. Skill `web-design-guidelines` (Vercel, MIT) u `.claude/skills/` je pomoćni alat za pregled UI-ja, nije dio aplikacije.
