# Data scout: Izazov C) Gospodarstvo i učinkovito upravljanje

_Provedeno 2026-10-22 (data-scout). Sve tvrdnje dolaze iz stvarnih GET odgovora (CKAN API, srdi.gov.hr, jpt.gov.hr, HNB, Eurostat) i uzoraka skinutih u privremeni direktorij izvan repoa. Što nije provjereno, označeno je **nepotvrđeno**. Portal povremeno vraća 502 (nekoliko upita je palo), pa pretraga nije iscrpna._

## Glavni nalaz u jednoj rečenici
Najjači materijal za C je **nacionalni EOJN registar ugovora (20 000 ugovora, 1335 naručitelja) spojen po OIB-u s registrom javnopravnih tijela (JPT, 4668 tijela)**. Oba su strojno dostupna, a OIB je stvarni ključ. Nije neobičan sam po sebi, ali spoj i način prezentacije mogu biti.

## Napomene o pristupu (zamke)
- Portal je pun **lokalnih "registara ugovora"/"planova nabave"** (84 skupa za "javna nabava", 208 za "plan nabave", 138 za "registar ugovora"), ali većina je HTML/PDF/DOCX ili po jedan XLSX za jednu općinu i godinu, bez ujednačene sheme. **Nisu spojivi bez ručnog čišćenja.** Za nacionalni pogled treba EOJN izvoz.
- `datastore_active=True` u CKAN-u nije jamstvo: npr. Finin RGFI i registar koncesija upućuju na web forme (`rgfi.fina.hr` traži registraciju, `servisi.fina.hr/regkonc`), ne na strojni download. **Nepotvrđeno** može li se ijedan skriptno povući.
- Windows: `python -I` ne razumije `/tmp/...` iz bash-a, treba `cygpath -m`. Naslovi i tagovi izgledaju kvarno u terminalu (prikazni problem).

## Kandidat-skupovi (izdvojeno 10)

### 1. EOJN registar ugovora (izvoz koji objavljuje Grad Zlatar, ali sadržaj je nacionalni)
- **Izdavatelj:** Grad Zlatar (ali sadrži 1335 različitih naručitelja, vidi niže). Poveznica: https://data.gov.hr/dataset/registar-ugovora-eojn-sustav
- **Resurs:** JSON, 28,7 MB, 20 000 redaka. URL: `https://data.gov.hr/ckan/dataset/7fd9ab5a-5bee-4034-81fa-be6ffe337c6b/resource/fe4cd062-809a-4f7e-a803-4aaa44c893de/download/pegistar-ugovora-eojn-sustav_2026-4-11-13-40-17.json`
- **Licenca:** otvorena dozvola RH (`open-license`).
- **Svježina:** `monthly`, zadnja izmjena 2026-05-14 ("do svibnja 2026."). Najstariji ugovori 2019., većina 2025–2026 (2024: 1508, 2025: 6807, 2026: 11635).
- **Ključ za spajanje:** `CAIdentificationNumber` (OIB naručitelja, 11 znamenki u 100% redaka) i `ContractorIdentificationNumber` (OIB izvođača, 98,8% valjanih 11 znamenki, 236 redaka strani/neispravni), `Cpv` (šifra + naziv, 100% popunjeno), `ContractDate`.
- **Polja koja vrijede:** `TotalValue`, `InitialValue`, `PayedAmount` (popunjeno 6803/20000), `ContractEUFinanc` (968 EU-financiranih ugovora, ~1219 M EUR), `ProcedureTypeName` (14 vrsta), `DurationFrom/To`, `TerminationDate` (5163).
- **Kvaliteta:** 18 098 jedinstvenih `Id` od 20 000 redaka (352 Id-a ponavljaju se zbog lotova, do 56×; razlikuju se po `LotId/TotalValue`). Datoteka je **vjerojatno odsječena na 20 000 redaka** (točno taj broj, redoslijed nije kronološki). **Nepotvrđeno** je li izvoz cjelovit ili uzorak. Uzorak je pristran: bolnice i velika državna poduzeća (KBC Zagreb 874, OB Pula 865, HEP ODS 489) pa nije reprezentativan za sve naručitelje. Nema polja o broju ponuda (ni jedne), pa se "jedan ponuditelj" ne može izravno izvesti.
- **Dohvat:** izravan HTTP download. Nije pravi API (`datastore_active=False`). Izvor `eojn.hr` nije bio dohvatljiv iz ovog okruženja (curl 000). **Nepotvrđeno** postoji li javni EOJN API za svježe ugovore.

### 2. JPT: Popis oznaka javnopravnih tijela (Ministarstvo pravosuđa)
- **Izdavatelj:** Ministarstvo pravosuđa, uprave i digitalne transformacije. Poveznica: https://data.gov.hr/dataset/popis-javnopravnih-tijela
- **Resurs:** pravi REST API (JSON), bez autentikacije: `https://jpt.gov.hr/Backend_Prod/api/PopisOznakaNaDanasnjiDan` (4,5 MB, 4668 tijela, HTTP 200), uz `.../api/Oznaka/{oib}` i `.../api/JavnoPravnoTijelo/{oznaka}`.
- **Licenca:** otvorena dozvola RH. **Svježina:** "na današnji dan" (zadnja izmjena skupa 2026-07-24), `refresh` nije naveden.
- **Ključ:** `OIB` (4668/4668 popunjeno), `Zupanija`, `GradOpcina`, `Naselje`, `PostanskiBroj`, adresa, `VrstaJPT` (>5 tipova), **`Osnivaci` (3888 tijela ima osnivače s OIB-om)**, `PravniSlijednici`, `DatumOsnivanja/Ukidanja`.
- **Kvaliteta:** čist, strukturiran, hijerarhija osnivača omogućuje lanac vlasništva javnog sektora. Sadrži e-mail i adrese ustanova (kontakti pravnih osoba, ne građana).
- **Dohvat:** **pravi API**, što je ono što kriterij "Upotreba otvorenih podataka" nagrađuje.

### 3. SRDI: Središnji registar državne imovine, financijska imovina
- **Izdavatelj:** Ministarstvo pravosuđa. Poveznica: https://data.gov.hr/dataset/sredisnji-registar-drzavne-imovine-financijska-imovina
- **Resurs:** javni REST API (OpenAPI/Swagger na `https://srdi.gov.hr/api/docs`, specifikacija `https://srdi.gov.hr/api/srdipb-public.yaml`). Lista: `GET /api/financijskaImovina?page.pageNum=0&page.pageSize=N` (JSON `{"data":[...],"totalCount":21}`), izvoz JSON/CSV/XML. Isto za `/nekretnine`, `/pokretnine`, `/prava`.
- **Licenca:** otvorena dozvola RH. **Svježina:** `continuous`, izmjena 2026-10-14.
- **Filtri:** `zupanija`, `gradOpcina`, `naselje`, `adresa`, `oibSubjekta`, `nazivSubjekta`, `vlasnik`, `obveznik`.
- **Kvaliteta:** **trenutno premalen skup**: financijska imovina ima samo **21 zapis** (17 Općina Stari Jankovci, 2 Plinacro). Registar se tek puni. Odgovor lista-endpointa ima `snake_case` polja, a izvoz drukčija (`Upravitelj`, `Naziv subjekta`). Nekretnine/pokretnine/prava **nepotvrđeno** (nisu uzorkovani). Pojedinosti o veličini popunjenosti treba provjeriti prije oslanjanja.
- **Ključ:** `oibSubjekta` je filtar, ali **OIB subjekta nije u izvezenim poljima** u uzorku (nepotvrđeno za ostale endpointe), pa je spoj preko naziva.

### 4. Informacije o ugovorenim koncesijama (Fina, Ministarstvo financija)
- **Poveznica:** https://data.gov.hr/dataset/informacije-o-ugovorenim-koncesijama. **Licenca:** otvorena dozvola RH. **Svježina:** `daily`.
- **Resurs:** vodi na web servis `http://servisi.fina.hr/regkonc/index.do` (oznaka CSV, `datastore_active=True`). Podaci uključuju OIB (prema opisu). **Nepotvrđeno** može li se strojno povući, nisam skidao. Ako može, to je najbolji kandidat za spoj s OIB-om.

### 5. Registar godišnjih financijskih izvještaja, javna objava (Fina)
- **Poveznica:** https://data.gov.hr/dataset/registar-godisnjih-financijskih-izvjestaja-javna-objava. Licenca: otvorena dozvola. Svježina: `yearly`.
- **Zamka:** resurs je stranica za prijavu korisnika (`rgfi.fina.hr/JavnaObjava-web/jsp/prijavaKorisnika.jsp`) i traži **registraciju**, dakle nije strojno dostupan. Ne računati na njega bez provjere uvjeta.

### 6. Podaci o poslovnim subjektima / Registar poslovnih subjekata (DZS)
- **Poveznice:** `podaci-o-poslovnim-subjektima` i `statistika-u-nizu-registar-poslovnih-subjekata`. Resursi su HTML (opisi/upute) i jedan XLSX ("Statistika u nizu", NKD 2007., agregirane tablice). **Nema mikropodataka po subjektu ni OIB-a.** Licenca XLSX-a: nije navedena (`None`). Svježina nenavedena (resursi 2023.).
- Korisno samo za NKD agregate, **ne za spoj po subjektu**.

### 7. Subvencije poduzetnicima Grada Rijeke (lista korisnika)
- **Poveznica:** https://data.gov.hr/dataset/subvencije-poduzetnicima-grada-rijeke-lista-korisnika. Licenca CC-BY. Resursi CSV/XLS na `data.rijeka.hr`, ali **samo za 2018.** i statistika 2008–2018. Zastarjelo (zadnja izmjena resursa 2021.). Slično imaju Zagreb ("potpore ...") i općine. **Nepotvrđeno** sadrže li OIB-ove korisnika.

### 8. Poslovne zone u RH (Agencija za investicije i konkurentnost)
- **Poveznica:** https://data.gov.hr/dataset/poslovne-zone-u-republici-hrvatskoj. Resursi: JSON `http://www.aik-invest.hr/wp-admin/admin-ajax.php?action=get_zones&lang=hr` (i `lang=en`). Licenca: `None`. Zadnja izmjena 2023-03-02. **Nepotvrđeno** sadržaj i koordinate (nisam skidao). Moguće spojiti prostorno na općine, ali je stariji podatak.

### 9. Lokalni proračuni (primjeri strojno čitljivih)
- `rashodi-proracuna-grada-bjelovara-funkcijski` i `...-ekonomski`: CSV + JSON izravno s `proracun.bjelovar.hr/town/data/funds.json`. Licenca: nije navedena. Zadnja izmjena 2023.
- `proracun-grada-rijeke-na-4-razini-konta` (XLS/XLSX, CC-BY), Zagreb izvršenja (CSV, do 2017.), Varaždin konsolidirani 2020/2021 (CSV).
- **Zamka:** svaka općina ima drukčiju shemu, razine konta i godine. Nema jedinstvenog nacionalnog izvršenja proračuna lokalnih jedinica u ovom katalogu (**nepotvrđeno**, dio pretraga pao s 502).

### 10. Vanjski izvori (izvan portala)
- **HNB tečajna lista:** `https://api.hnb.hr/tecajn-eur/v3?valuta=USD` vraća JSON (HTTP 200, datum primjene 2026-10-23). Pravi API, dnevno. Korisno samo za normalizaciju valuta. **Licenca/uvjeti: nepotvrđeno** (provjeriti uvjete HNB-a prije oslanjanja).
- **Eurostat dissemination API:** `https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/nama_10r_3gdp?geo=HR031&unit=MIO_EUR` vraća JSON-stat (HTTP 200, regionalni BDP po NUTS3, HR031 2 vrijednosti). Pravi API, otvoreno. Eurostat ponovna uporaba je uz navođenje izvora (**licenca nepotvrđena** za ovaj skup). Jedan pokušaj (`bd_9ac_l_form_r3`) vratio je 404, pa kodove skupova treba provjeriti.
- **Sudski registar otvoreni podaci (`sudreg-data.gov.hr/api/javni/`):** HTTP **401 Unauthorized**, traži autorizaciju. Nije dostupan bez registracije. Ne računati na njega.
- **TED (EU javna nabava) `api.ted.europa.eu`:** vratio HTML početnu (200). **Nepotvrđeno** radi li anonimni pretraživački API i pokriva li HR iznad pragova. Kandidat za usporedbu cijena s EU razinom.

## Neobični spojevi (provjereno na uzorku)

### Spoj 1: Naručitelji iz EOJN-a × JPT registar po OIB-u (karta javne potrošnje po osnivaču/tipu/županiji)
- **Ključ:** `CAIdentificationNumber` (EOJN) = `OIB` (JPT).
- **Pokrivenost (uzorak 18 098 jedinstvenih ugovora):** 975 od 1337 naručitelja (**72,9%**) spojeno; **67,0% ugovora** i **55,1% vrijednosti** pokriveno. Spoj dodaje županiju, grad/općinu, tip tijela i osnivače.
- **Što se ne spaja:** veliki dio nespojenih su javna poduzeća i bolnice kojih nema u JPT-u kao pravnih osoba s javnim ovlastima (HEP ODS 489, OB Pula 419, KBC Zagreb 186, HEP d.d. 146). Zato **bolnice i energetika ispadaju**, a oni su među najvećim potrošačima.
- **Nova informacija:** potrošnja javnog sektora **po tipu tijela, županiji i lancu osnivača**, koje EOJN sam ne daje. 21 županija pokrivena. Najveće: Grad Zagreb 514,8 M EUR, Splitsko-dalmatinska 211,8 M, Primorsko-goranska 93,0 M (u uzorku).
- **Procjena neobičnosti:** srednja. Ideja "karta javne nabave" je česta, ali **lanac osnivača** (tko tko osniva, pa kome idu novci) nije.

### Spoj 2: EOJN × JPT: tok javno→javno i koncentracija izvođača
- **Ključ:** `ContractorIdentificationNumber` = JPT `OIB`.
- **Pokrivenost:** 547 ugovora (**3,0%**) ima izvođača koji je i sam javno tijelo (javno→javno tok). Mali udio, zato nije glavna tema.
- **Koncentracija:** 6611 izvođača; **800 radi za ≥3 naručitelja, 80 za ≥10**. Vodeći po broju različitih naručitelja: HP (240), HEP-Opskrba (170), INA (158), Telemach (81), Croatia osiguranje (74). Po broju redaka: Medika (1407), Phoenix Farmacija (857), Oktal Pharma (728), što je farmacija za bolnice (CPV 33 = 4551 redaka).
- **Napomena:** uzorak je pristran na bolnice (vidi kvalitetu), pa koncentraciju ne treba tumačiti kao nacionalnu. Za **pošteno** tvrđenje treba cjelovit izvoz.

### Spoj 3: EOJN × Eurostat/DZS po županiji (javna potrošnja per capita ili u odnosu na regionalni BDP)
- **Ključ:** JPT `Zupanija` ⇒ NUTS3 (HR031...). Eurostat `nama_10r_3gdp` radi (HTTP 200).
- **Pokrivenost:** 21 županija iz EOJN-a spojeno preko JPT-a. **Ali** samo 55,1% vrijednosti, pa je rezultat nepotpun. Mapiranje naziv županije→NUTS3 je ručno (**nije provjereno**), i treba stanovništvo (nije u ovom istraživanju).
- **Procjena neobičnosti:** srednja do visoka, ali **izvedivost srednja** jer treba ručno kodiranje i cjelovit izvoz.

### Spoj 4: EOJN × EU-financirano × (planirano) fondovi
- **Ključ:** `ContractEUFinanc=True` (968 ugovora, ~1219 M EUR). Spoj s fondovima/projektima **nema zajednički ključ** u onome što sam našao (lokalni "EU projekti" skupovi su HTML bez OIB-a, 28 pogodaka). Zato je ovo samo **filter unutar EOJN-a**, ne pravi spoj.
- **Procjena:** niska za spoj, ali "praćenje novca iz EU fondova u nabavi" je koristan filter u UI-ju.

### Spoj 5 (hipoteza, nepotvrđeno): ugovori × koncesije po OIB-u
- Ako se Finin registar koncesija (točka 4) može strojno povući i sadrži OIB, spoj s EOJN izvođačima i JPT davateljima dao bi "tko dobiva i ugovore i koncesije". **Ništa od toga nisam mogao provjeriti** (resurs je web forma). Označeno kao rizik, ne kao plan.

## Realno koliko se ovdje može naći nešto neobično
**Malo.** Izazov C je skoro sigurno zasićen: "javna nabava", "pregled ugovora" i "transparentnost proračuna" su prvo što će tim pomisliti. Moja procjena:
- **Podaci su dobri, ali ne i rijetki.** EOJN + JPT su strojni, imaju OIB i nisu skriveni, pa će ih naći i drugi timovi. Prednost se ne dobiva samim spojem, nego (a) cjelovitošću (provjeriti je li izvoz odsječen na 20 000), (b) lancem osnivača i javno→javno tokovima, (c) kvalitetom prikaza i demoa (UI/UX 10 + video 10 bodova).
- **Najslabija karika je dohvat svježih ugovora.** Jedini nacionalni izvoz je indirektan (preko Zlatara, mjesečno), a EOJN nije bio dohvatljiv. Bez toga "Upotreba otvorenih podataka" (izravno preko API-ja) dobiva slabije bodove za EOJN dio, dok JPT i SRDI to pokrivaju.
- **Pravni/GDPR rizik:** izvođači mogu biti obrti i fizičke osobe (neki OIB-ovi su osobni). Označiti i izbjegavati analizu pojedinaca.
- **Preporuka:** C nije najbolji izbor za unikatnost. Ako se ipak ide na C, tražiti **neobičan kut** (npr. "tko su osnivači tijela koja nabavljaju" ili otkrivanje nabave izuzete od ZJN-a: 9878 od 20 000 redaka, ali samo ~3% vrijednosti), a ne generičku nadzornu ploču. Preporučujem prvo pokrenuti scout za A i B i usporediti.

## Što još treba provjeriti (prije odluke)
1. Je li EOJN izvoz cjelovit ili odsječen na 20 000 redaka, i postoji li drugi nacionalni izvoz ili API (kontaktirati open.data@mpudt.hr).
2. Mogu li se Fina RGFI i registar koncesija strojno povući bez registracije.
3. Obuhvat i OIB-ovi u ostalim SRDI endpointima (nekretnine, pokretnine, prava) kad se registar napuni.
4. Uvjeti ponovne uporabe HNB-a i Eurostata za konkretne skupove.
