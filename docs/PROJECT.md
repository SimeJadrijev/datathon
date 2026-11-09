# OtvoreniRadar: projektna dokumentacija

**Tim Nazor** (Ivan Mosić, Šime Jadrijev, Luka Žuljević, Nino Milas; mentor Mario Čagalj)
Datathon Portala otvorenih podataka 2026, **izazov C: Gospodarstvo i učinkovito upravljanje**

> Svi javni natječaji, pozivi i propisi RH na jednom mjestu. Upišeš OIB i vidiš koji natječaji ti odgovaraju i zašto, a za svaki možeš u minuti provjeriti ispunjavaš li uvjete.

Tehnički detalji su u [`ARCHITECTURE.md`](ARCHITECTURE.md), izvori podataka u [`DATA_SOURCES.md`](DATA_SOURCES.md), a pokretanje u [`README.md`](../README.md).

## 1. Problem

Javni novac za udruge, tvrtke, obrtnike, poljoprivrednike, studente i građane dijeli se kroz stotine natječaja godišnje. Informacija o njima je razbacana i teško upotrebljiva:

- **Previše mjesta.** U našoj snimci od 4. 11. 2026. otvorene natječaje objavljuje **66 različitih tijela**, a ukupno smo zabilježili natječaje **205 izdavatelja**: ministarstva, agencije, fondovi, gradovi, županije, sveučilišta. Svako tijelo ima vlastitu stranicu i vlastiti format.
- **Nema strojno čitljivih podataka.** Na Portalu otvorenih podataka ne postoji skup podataka s aktualnim natječajima. Natječaji se objavljuju kao web vijesti i PDF-ovi (dio njih i kao skenirani dokumenti).
- **Ključni podaci su zakopani u tekstu.** Rok, iznos i tko se smije prijaviti često su samo u slobodnom tekstu ili u prilogu od 20 stranica.
- **Uvjeti su nejasni.** Da bi saznao smije li se prijaviti, korisnik mora pročitati cijeli poziv i propise na koje se poziv poziva.
- **Propisi su odvojeni od natječaja.** Natječaj navodi „Zakon o … (NN 74/14)”, a korisnik ga mora sam pronaći u Narodnim novinama.

Posljedica: male organizacije bez zaposlenika za projekte propuštaju sredstva ili troše dane na traženje i čitanje. Novac odlazi onima koji imaju ljude za praćenje natječaja, a ne nužno onima kojima je namijenjen. Izazov C traži upravo smanjenje vremena i troška pronalaženja i obrade informacija te povezivanje administrativnih i gospodarskih podataka.

## 2. Ciljani korisnici

| Korisnik | Potreba | Što dobiva |
|---|---|---|
| **Udruga** (najviše otvorenih poziva: 73 od 157) | pratiti pozive ministarstava, Ureda za udruge, gradova i županija | profil iz OIB-a preko Registra udruga, preporuke po području djelovanja i županiji |
| **Tvrtka i obrt** (41 + 18 poziva) | naći potpore za svoju djelatnost, EU fondove i lokalne potpore | profil iz OIB-a preko Sudskog registra (sjedište, djelatnost NKD), preporuke |
| **Poljoprivrednik** (16) | pratiti APPRRR i Ministarstvo poljoprivrede | filtar i preporuke za poljoprivredu i ruralni razvoj |
| **Student** (16) | stipendije, Erasmus, pozivi sveučilišta i studentskih centara | preporuke za studente iz izvora sveučilišta |
| **Građanin** (24) | potpore za energetsku obnovu, e-automobile, stanovanje | jednostavan profil (županija i tema) bez OIB-a |
| **Jedinica lokalne samouprave** (31) | pozivi FZOEU-a, EU fondova i ministarstava | preporuke za JLS |
| **Javna uprava** (sekundarni korisnik) | pregled što se objavljuje, gdje i u kakvom obliku | export u standardu, Atom feed, prijedlog standarda za objavu natječaja |

## 3. Rješenje

OtvoreniRadar svaki dan sam dohvaća natječaje iz 80-ak izvora javnog sektora i propise iz Narodnih novina. Iz teksta izvlači ključne podatke, povezuje ih s profilom korisnika i s propisima i prikazuje ih u jednostavnom sučelju.

### Glavne funkcije

1. **Profil iz OIB-a.** Korisnik upiše OIB, a aplikacija iz Sudskog registra (tvrtke) ili Registra udruga (udruge) popuni sjedište, županiju i djelatnost. Bez OIB-a profil se složi s tri klika (tko si, gdje si, što te zanima).
2. **Preporuke s objašnjenjem.** Natječaji su poredani po tome koliko odgovaraju profilu, a uz svaki piše i zašto („lokalno: Splitsko-dalmatinska”, „djelatnost NKD 62”, „rok za 12 dana”).
3. **Svi natječaji na jednom mjestu.** Pretraga i filtri po statusu, roku, iznosu, ciljnoj skupini, županiji, području i izvoru. Duplikati objavljeni na više mjesta prikazuju se jednom.
4. **Detalj natječaja.** Rok, iznosi, tko se može prijaviti, sažetak uvjeta i potrebnih dokumenata, privitci, poveznica na izvornu objavu i pravna osnova s poveznicom na propis.
5. **„Ispunjavam li uvjete?”** Korisnik odgovori na do 5 kratkih pitanja izvedenih iz teksta natječaja. AI vrati procjenu (vjerojatno ispunjavaš / ne ispunjavaš / nejasno) s **doslovnim citatima** iz natječaja. Ako model nije dostupan, odgovor se daje iz pravila (rok, ciljna skupina, područje).
6. **Propisi.** Novi propisi iz Narodnih novina s kratkim AI sažetkom i popisom natječaja koji se na njih pozivaju.
7. **Praćenje bez prijave.** Atom/RSS feed za bilo koju kombinaciju filtara („novi natječaji za studente u Splitsko-dalmatinskoj”).
8. **Transparentnost izvora.** Stranica „Izvori” pokazuje svaki izvor, kad je zadnji put dohvaćen i koliko je zapisa donio. AI podaci su u sučelju uvijek označeni i odvojeni od službenih.

### Primjer korištenja

Mala tvrtka iz Zagreba upiše svoj OIB. Aplikacija iz Sudskog registra prepozna sjedište i djelatnost i pokaže desetak otvorenih poziva: EU poziv iz eKohezije, potporu Grada Zagreba i poziv ministarstva. Za prvi poziv korisnik klikne „Ispunjavam li uvjete?” i odgovori na četiri pitanja. Za manje od minute dobije „Vjerojatno ispunjavaš” s četiri citata iz poziva i poveznicom na izvornu objavu. Bez OtvoreniRadara bi za isto morao obići desetke stranica i pročitati nekoliko PDF-ova.

## 4. Podaci

Rješenje koristi isključivo otvorene podatke i javno objavljene informacije javnog sektora:

- **Portal otvorenih podataka:** Registar udruga (CKAN API) i Sudski registar (API, skup podataka na data.gov.hr).
- **API-ji javnog sektora:** eKohezija (svi pozivi EU fondova 2021.-2027.), HZZ mjere, Narodne novine (ELI).
- **Javne objave** 18 ministarstava i ureda Vlade, agencija i fondova (FZOEU, APPRRR, HRZZ, AMPEU), 36 gradova i županija te sveučilišta i studentskih centara.

Stanje snimke od 4. 11. 2026.: **1.222 natječaja** (157 otvorenih, za 155 su pripremljena pitanja o uvjetima) i **4.121 propis**. Otvoreni pozivi koji navode iznos (95 od 157) zajedno raspolažu s više od **2,5 milijarde eura**. Puni popis izvora s licencama je u [`DATA_SOURCES.md`](DATA_SOURCES.md).

**Nova informacijska vrijednost** nastaje iz spajanja: OIB → Sudski registar / Registar udruga → djelatnost i županija → odgovarajući natječaji → propisi na kojima se temelje. Nijedan od ovih izvora sam ne odgovara na pitanje „koji natječaji su za mene i smijem li se prijaviti”.

## 5. Vrijednost i utjecaj

**Za korisnike**
- Manje vremena za traženje: jedno mjesto i jedan profil umjesto desetaka stranica.
- Manje propuštenih prilika: feed i oznaka „novo” upozoravaju na nove pozive.
- Brža odluka o prijavi: provjera uvjeta s citatima umjesto čitanja cijelog poziva.
- Jednak pristup: male udruge, obrti i studenti dobivaju informacije koje su do sada imali samo oni s ljudima za praćenje natječaja.

**Za javnu upravu**
- Veći doseg poziva, a time i više kvalitetnih prijava i manje poziva bez prijavitelja.
- Uvid u to koliko je objava natječaja neujednačena, uz konkretan **prijedlog standarda** „Otvoreni podaci o natječajima” (JSON Schema i export već rade).
- Gotov izvoz i feed koje mogu preuzeti Portal otvorenih podataka, gov.hr ili lokalne samouprave.

**Šire**
- Bolja iskorištenost EU i nacionalnih sredstava, što je izravno u interesu RH.
- Pokazuje kako AI može učiniti postojeće javne informacije upotrebljivima, bez novog posla za tijela koja ih objavljuju.

## 6. Inovativnost

- **AI za strukturiranje javnih objava:** model iz slobodnog teksta i skeniranih PDF-ova izvlači rok, iznose i uvjete po fiksnoj shemi. Podaci iz izvora uvijek imaju prednost pred modelom.
- **Provjera uvjeta s citatima:** odgovor nije „vjeruj nam”, nego procjena s doslovnim navodima iz natječaja, uz pravila kao rezervu kad model nije dostupan.
- **Automatska pravna osnova:** prepoznaju se navodi propisa u tekstu natječaja i povezuju s Narodnim novinama preko ELI-ja.
- **Objašnjive preporuke:** svaki rezultat kaže zašto je preporučen.
- **Odgovorno korištenje:** poštuje se `robots.txt`, izvori koji ne žele AI obradu se izostavljaju, a AI podaci su jasno označeni.

## 7. Izvedivost i stanje razvoja

- **Radni prototip:** web aplikacija + API + baza. Radi i bez AI ključa i bez interneta, iz snimke baze u repozitoriju.
- Više od 400 automatskih testova API-ja, a provjera tipova i lint za web prolaze.
- Dnevni automatski dohvat, dnevnik po izvoru, budžet za AI. Obrada cijele baze AI-jem stajala je manje od 1 USD.
- Kod je otvoren (MIT) i modularan: novi grad s WordPress ili Joomla stranicom dodaje se jednim retkom konfiguracije.

## 8. Daljnji razvoj

**Kratkoročno (1-3 mjeseca)**
- Javna instanca i obavijesti mailom uz postojeći feed.
- Proširenje na sve gradove i županije preko istih generičkih fetchera.
- Ručna provjera točnosti AI izvlačenja na uzorku i prikaz izmjerene točnosti.

**Srednjoročno (3-12 mjeseci)**
- Objava izvoza kao skupa podataka na data.gov.hr i dogovor s nekoliko tijela da objavljuju natječaje u predloženom standardu.
- Prijava preko NIAS-a (e-Građani) za spremanje profila, praćenje prijava i podsjetnike na rok.
- Model koji radi na vlastitom poslužitelju za obradu u javnoj upravi.

**Dugoročno**
- Uključivanje u gov.hr ili Portal otvorenih podataka kao javna usluga.
- Povezivanje s podacima o dodijeljenim sredstvima (tko je dobio novac), za transparentnost i za bolje preporuke („slične udruge su dobile ovaj poziv”).

## 9. Ograničenja (iskreno)

- Dohvat s web stranica je krhak. Kad izvor promijeni stranicu, parser treba prilagoditi.
- Pokrivenost lokalne razine je djelomična: 36 od 576 jedinica lokalne i područne samouprave.
- Dio natječaja nema rok u tekstu (npr. mjere „do iskorištenja sredstava”).
- AI procjena uvjeta je informativna, ne pravni savjet. Sučelje to kaže i uvijek vodi na izvorni tekst.
- Licence za dio izvora koji se čitaju s web stranica nisu izričito navedene (vidi [`DATA_SOURCES.md`](DATA_SOURCES.md)).

## 10. Tim

| Član | Uloga |
|---|---|
| Šime Jadrijev | podaci i AI |
| Ivan Mosić | baza i podatkovni pipeline, web |
| Luka Žuljević | izvori, pouzdanost dohvata, DevOps |
| Nino Milas | frontend, UX i integracija |

Mentor: Mario Čagalj. Korišteni AI alati navedeni su u [`README.md`](../README.md#korišteni-alati).
