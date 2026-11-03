# Istraživanje dodatnih izvora natječaja (3.11.2026.)

Pregledano oko 40 kandidata. [V] znači da je izvor dohvaćen i provjeren (uključujući robots.txt), a [S] da podatak dolazi samo iz pretrage.

## Dodano
- **eKohezija** [V]: `https://ekohezija.gov.hr/MISPublicApi/poziv/browse/?top=100&skip=0`.
  - Javni JSON API iza eufondovi.gov.hr, bez autentikacije i bez robots.txt.
  - Pokriva svih 398 poziva iz programa 2021.-2027. (PK, IP, SF): 107 natječajnih, 17 otvorenih na dan 3.11.
  - Statusi su `Otvoren` i `Završen`.
  - Izravne dodjele (`Izravna dodjela – strateški projekt`, `Izravna dodjela`) se preskaču, jer nisu pozivi za javnost.
  - Prijavitelji, iznosi i rokovi su strukturirani.
  - Nema preklapanja s NPOO (fondovieu.gov.hr).

## Kandidati za kasnije (redoslijed po vrijednosti i trudu)
| Izvor | Pristup | Publika | Napomena |
|---|---|---|---|
| HRZZ [V] | WP-REST `hrzz.hr/wp-json/wp/v2/posts?categories=39` | znanstvenici, doktorandi | 80 objava, nekoliko otvorenih |
| AMPEU [V] | HTML `ampeu.hr/natjecaji`, `/p2`, `/p3` | studenti, škole (Erasmus+) | 5 aktivnih od 30 |
| euribarstvo.hr [V] | WP-REST `posts?categories=15` | ribari | oko 4 godišnje |
| Grad Zagreb [V] | HTML `zagreb.hr/natjecaji/860?godina=aktualno` | građani, stipendije | lista miješa zapošljavanje i najam, treba filter naslova |
| Osijek, PGŽ, KZŽ [V] | WP-REST custom post types (`natjecaji`, `natjecaj`) | građani, studenti, kultura | udruge se preklapaju s udruge.gov.hr |
| Nacionalna zaklada za razvoj civilnoga društva [V] | HTML s paginacijom | udruge | djelomično na udruge.gov.hr |
| EU Funding & Tenders (SEDIA) [V] | `POST api.tech.ec.europa.eu/search-api/prod/rest/search?apiKey=SEDIA` | MSP, NGO, znanost | na engleskom, 1200 poziva; vrijedi samo za cascade funding (tip 8) |
| HAVC [V] | jedna HTML stranica | film, igre | 14 stalnih kategorija |
| Zaklada hrvatskih branitelja [V] | WP-REST | studenti | 1-2 godišnje |

## Odbačeno
- HAMAG-BICRO: robots.txt izričito blokira AI botove (`Content-Signal: ai-train=no`), a bespovratna sredstva su ionako u eKoheziji.
- ruralnirazvoj.hr: isti naslovi kao APPRRR.
- strukturnifondovi.hr: samo arhiva 2014.-2020.
- LAG-ovi: oko 56 zasebnih stranica, nema središnjeg popisa.
- Ministarstva na Vladinom CMS-u: natječaji za udruge su već na udruge.gov.hr, a detalji su u PDF-u.
- HBOR (krediti), HOK i HGK (bez natječaja za financiranje).

## data.gov.hr (CKAN)
Nijedan skup ne nabraja otvorene natječaje. Za buduću značajku "tko je dobio sredstva" postoje skupovi o dodijeljenim sredstvima:
- Ured za udruge, financiranje udruga (XLS, do 2023.);
- MRRFEU, popis operacija 2014.-2020.;
- Rijeka, subvencije poduzetnicima (CSV).
