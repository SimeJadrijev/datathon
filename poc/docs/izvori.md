# Izviđanje izvora (24. 10. 2026.)

Za svaki izvor: struktura, robots.txt, odabrani pristup. User-Agent `DatathonPOC/0.1 (studentski projekt)`, najviše 1 zahtjev/s po domeni, bez prijave i bez zaobilaženja zaštite. Nijedan izvor nije tražio login ni CAPTCHA. Nijedan nije trebao JS (Playwright nije potreban).

## 1. APPRRR (apprrr.hr)
- **robots.txt**: zabranjuje samo `/wp-admin/`.
- **RSS** `https://www.apprrr.hr/feed/` radi, ali sadrži samo **10 zadnjih objava** svih kategorija (novosti, zapošljavanje, isplate...).
- **WordPress REST API je javan**: `/wp-json/wp/v2/categories` (kategorije `natjecaji` id 155, `obavijesti` 167, `potpore` 157 je prazna) i `/wp-json/wp/v2/posts?categories=155&per_page=100`. Vraća puni HTML sadržaj objave.
- Objava je najava ("Objavljen natječaj za intervenciju 73.10..."), a **dokumentacija je na stranici intervencije** (`/intervencije/...`), na koju objava linka. Stranica intervencije sadrži PDF-ove svih natječaja te intervencije, pa ih treba razvrstati.
- **Pristup**: RSS za svježe + REST API za arhivu; detalj = stranica intervencije.

## 2. FZOEU (fzoeu.hr)
- **robots.txt**: zabranjuje 2 konkretna PDF-a.
- `https://www.fzoeu.hr/hr/natjecaji/7` iz upute **preusmjerava (302)** na stranicu "Energetska obnova obiteljskih kuća". Pravi popis je **"Nacionalni javni pozivi i natječaji"** `/hr/nacionalni-javni-pozivi-i-natjecaji/1367`.
- Sve na jednoj stranici: accordion (`li.accordion-header` + `li.accordion-hidden`) s naslovom, statusom (`div.status.status-N > span`: otvoren/zatvoren), "Otvoren: datum", opisom i popisom dokumenata (`ul.documents-list`, folder `/docs/<ID>/`). ID foldera je stabilan ključ.
- Stranica "Zatvoreni natječaji" (`/hr/zatvoreni-natjecaji/9599`) nema strojno čitljiv popis. Postoji i XLSX "Indikativni plan poziva i natječaja za 2026." (najavljeni pozivi, potencijalno dobar izvor za status "najavljen").
- **Pristup**: statični HTML, jedna stranica; PDF poziva za rok i iznose.

## 3. EU fondovi (fondovieu.gov.hr)
- **robots.txt**: zabranjuje `/download`, `/prijavitelj`, `/korisnik`, `/registracija`... -> **prilozi poziva se ne smiju preuzimati**.
- Nuxt aplikacija sa **server-side renderingom**: HTML popisa sadrži sve podatke bez JS-a. Paginacija `?page=N&statusGroup=otvoreni|zatvoreni|svi` (10 po stranici). Kartica: kod poziva, naziv, tip natječaja, rok.
- Detalj `/pozivi/<id>` (SSR, ~500 KB): kod, datum objave, rok, nadležno tijelo, svrha, prihvatljivi prijavitelji.
- XHR prema API-ju nije uočen u pregledniku (podaci dolaze iz SSR-a); interni API nije javno dokumentiran pa ga POC ne koristi.
- Portal pokriva **NPOO**; pozivi iz programa 2021.-2027. su na **eufondovi.gov.hr** (HAMAG linka tamo). To je dodatni izvor za konačni proizvod.

## 4. HAMAG-BICRO (hamagbicro.hr)
- **robots.txt**: zabranjuje `/arhiva/`, `/mapa-weba/`, `/wp-admin/`...
- WordPress, ali **REST API vraća 404** (isključen). Popis `/natjecaji/page/N/` (6 stranica), masonry grid `div.post-wrapper` s kategorijama u CSS klasama (`category-otvoreni-natjecaji`, `category-vijesti-i-najave`), datumom i naslovom. `/otvoreni-natjecaji/` je filtrirani pogled.
- Dio kartica vodi izravno na `eufondovi.gov.hr/poziv/?id=...`.
- **Pristup**: statični HTML + paginacija; detalj = stranica objave.

## 5. HZZ mjere (mjere.hr -> mjere.hzz.hr)
- `mjere.hr` preusmjerava (301) na `mjere.hzz.hr`. **robots.txt**: zabranjuje `/wp/wp-admin/`.
- **WordPress REST API**: tip `measures` (97 mjera od 2021.), taksonomije `measures_years` (godina) i `measures_category` (Poslodavac, Nezaposlen, Samozapošljavanje...). Cijeli tekst u `content`.
- Mjere su **kontinuirane** (prijava tijekom godine), bez roka i bez ukupnog iznosa.
- **Pristup**: REST API, jedan zahtjev.

## 6. Ured za udruge (udruge.gov.hr)
- **robots.txt**: nema (404).
- Središnji **"Pregled natječaja"** za udruge od svih davatelja (ministarstva, JLS, zajednice): `/istaknute-teme/financiranje-programa-i-projekata-udruga-iz-javnih-izvora/natjecaji-5503/5503`, filtar `status=1` (otvoreni, ~97) / `status=2` (zatvoreni, ~3.250), paginacija `page=N`.
- Detalj `?nid=<id>` je **strukturiran**: institucija, razina, županija, naziv, datum objave, **datum zatvaranja**, **ukupni / najniži / najviši iznos**, poveznica na izvorni natječaj. Ovo je najbliže "otvorenim podacima o natječajima" što postoji.
- Isti Ured na data.gov.hr objavljuje **"Podaci o financiranju udruga"** (XLSX, tko je dobio, s OIB-om).
- **Pristup**: statični HTML + paginacija; detalj strukturirani obrazac.

## 7. Stipendije
### MZOM (mzom.gov.hr)
- **robots.txt**: nema (404). Isti CMS kao udruge.gov.hr.
- `/istaknute-teme/natjecaji-196/196` -> popis s paginacijom (`/natjecaji-196/196?trazi=1&page=N`, ~1.550 stavki, 155 stranica). Stavka: naziv, **"Rok:"**, datum, tip (Natječaji, Javni pozivi...), tema (Stipendije...). Miješa i oglase za posao (filtriranje po naslovu).
- Posebna stranica "Državne stipendije" `/.../drzavne-stipendije/162`.
### AMPEU (ampeu.hr)
- **robots.txt**: zabranjuje `/cpresources/`, `/vendor/`, `/.env`, `/cache/`.
- Craft CMS, `/natjecaji` + `/natjecaji/pN`, kartice `div.NewsItem` s programom, `<time datetime>`, naslovom i "Rok je prošao" za zatvorene.
- Veliki dio su pozivi institucijama (Erasmus+ akreditacije) i radionice; stipendije pojedincima su manji dio (Bilateralni program, CEEPUS).
- Europske snage solidarnosti su na zasebnoj domeni (europskesnagesolidarnosti.hr), nije obuhvaćeno.

## 8. Gradovi
### Rijeka (rijeka.hr)
- **robots.txt**: sve dopušteno. WordPress s javnim REST API-jem (posts), ali natječaji su tip `bidding` koji **nije** u REST API-ju.
- `/gradska-uprava/natjecaji-2/` ima podstranice: zakup prostora, prodaja, zapošljavanje, zemljište, **Ostali natječaji** (+ arhiva). Financijske potpore i stipendije se uglavnom objavljuju kao **vijesti**.
- **Pristup**: HTML stranice "Ostali natječaji" + REST pretraga vijesti (`/wp-json/wp/v2/posts?search=`).
### Osijek (osijek.hr)
- **robots.txt**: zabranjuje `/wp-admin/`, `/testiranje/`.
- **WordPress REST API s tipom `natjecaji`** (~1.040 zapisa). Sadržaj je u ACF poljima (`acf_fields.sadrzaj`, `datoteka`, `datoteke`) -> uglavnom PDF/DOCX. Tip miješa savjetovanja, oglase, prodaju zemljišta.
- **Pristup**: REST API + PDF prilozi.
### Zagreb (zagreb.hr)
- **robots.txt**: nema (404). Vlastiti CMS.
- Nema jedinstvenog popisa: "Natječaji / Pozivi / Rezultati", "Javni pozivi za poduzetnike", "Stipendije Grada Zagreba", "Potpore obrazovanju", eko.zagreb.hr... Svaka stranica ručno uređena, linkovi oblika `/naslov/ID`, popis u `div.newswrap`. Stare stavke (2024.) ostaju na stranicama.
- **Pristup**: nekoliko tematskih stranica; najveći trošak održavanja.

## Opcionalno (nije rađeno)
- Narodne novine (oglasni dio) i EOJN: izvan opsega (javna nabava, ne potpore).
- eufondovi.gov.hr: novi portal EU fondova 2021.-2027.; preporuka za sljedeći korak.

## Otvoreni podaci za povezivanje (CKAN API data.gov.hr)
- `podaci-o-financiranju-udruga` (Ured za udruge): XLSX po vrsti davatelja (državna tijela, županije, gradovi, općine...), stupci DAVATELJ, ORGANIZACIJA, **OIB**, PROJEKT, ŽUPANIJA, GODINA, IZNOS. Najbolji skup "tko je dobio".
- `subvencije-poduzetnicima-grada-rijeke-lista-korisnika`: samo 2018., CSV link vraća 404 (data.rijeka.hr).
- `potpore-poduzetnicima-grad-karlovac`: DOCX + linkovi na web, nije strojno čitljivo.
- `potpore-inovatorima-grada-zagreba`: XLSX/CSV, 2014.-2015.
- `popis-javnopravnih-tijela`: JSON API (jpt.gov.hr) -> kandidat za normalizaciju "davatelja".
- `registar-udruga`: JSON/CSV s djelatnostima (datastore aktivan) -> profil udruge po OIB-u.
- NKD 2025 (DZS): JSON/XML preko KLASUS-a (302 redirect).
- **Sudski registar**: OpenAPI javan, ali **svi endpointi traže OAuth2 client credentials** (registracija).
