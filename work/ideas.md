# Ideje

_Zadnje ažurirano: 2026-10-23. Samo ideje za izazove A i C (B je odbačen). Brojevi ideja su stabilni jer ih referenciraju `decisions.md` i `log.md`, pa rupe u numeraciji znače uklonjene ideje (vidi "Odbačene"). Bodovi NISU računati, `jury-simulator` nije pokrenut. Brojke su iz `context/scout/`, što nije provjereno piše nepotvrđeno._

Prag za nagradu je 50/60 (83%), pa svaka ideja mora biti iznadprosječno izvedena. Diplomski je Simeova želja, ne pravilo.

## Aktivne ideje

### 13. "Radar nabave" (C), FAVORIT
- **Problem:** mali poduzetnici gube vrijeme tražeći javne natječaje, a mnogi se ponavljaju (isti naručitelj, ista CPV šifra).
- **Feature:** opišeš što firma radi (hrvatski, slobodan tekst), sustav to mapira u CPV šifre i pokaže ugovore koji ističu u idućih 6 mjeseci, vjerojatnost i vrijeme ponovnog natječaja te procijenjenu vrijednost. Nadogradnja: planovi nabave iz ideje 15.
- **AI/DS:** predviđanje ponavljanja (survival/recurrence) po (naručitelj, CPV), hijerarhijska klasifikacija teksta u CPV, evaluacija backtestom.
- **Podaci:** EOJN registar ugovora (`DurationTo`, `Cpv`, OIB naručitelja 100%), JPT API.
- **Rizici:** EOJN izvoz je 20 000 redaka, pristran na bolnice i vjerojatno odsječen (do 05/2026). Popunjenost `DurationTo` nepotvrđena. Ponavljaju li se ugovori pri isteku nepotvrđeno. EOJN vjerojatno već ima obavijesti o aktualnim natječajima (nepotvrđeno), pa je naša razlika predviđanje.
- **Jeftini test:** na postojećem izvozu izmjeriti udio redova s `DurationTo` i poklapa li se početak sljedećeg ugovora (naručitelj, CPV) s istekom prethodnog.

### 14. "Pristupačnost po profilu korisnika" (A), FAVORIT
- **Problem:** "blizu" nije isto za 30-godišnjaka i 80-godišnjakinju.
- **Feature:** odabereš profil (starija osoba, kolica, roditelj s kolicima, prosjek) i karta pokaže što stvarno dohvatiš u 15 minuta s obzirom na nagib terena i pješačku mrežu. "Što ako": gdje bi nova ljekarna ili dom zdravlja najviše povećala dostupnost.
- **AI/DS:** analiza pješačke mreže (OSM), model brzine hoda po nagibu i profilu, optimizacija pokrivenosti (maximal coverage / p-median), analiza osjetljivosti.
- **Podaci:** zagrebački geoportal (škole 177, vrtići 320, ljekarne 245, zdravstvo 47, domovi za starije 85, ZET stajališta 2134, sve s koordinatama), OSM (ODbL), model terena (izvor i licenca nepotvrđeni).
- **Rizici:** samo Zagreb. Bez stanovništva po četvrti (nepotvrđeno gdje) nema ponderiranja potražnjom. Škole su medijan 157 m od stajališta, pa tu nema varijance (ljekarne i domovi nisu mjereni). Podataka o pristupačnosti (rampe, klupe) nema osim eventualnih OSM oznaka (pokrivenost nepotvrđena). Kartu okoline će graditi i drugi, pa razlika mora biti profil + optimizacija.
- **Jeftini test:** dohvatiti OSM pješačku mrežu Zagreba i provjeriti je li dovoljno gusta za izračun 15-minutnih dosega.

### 15. "Odblokiraj podatke": AI za tablice u PDF/XLS (C)
- **Problem:** na portalu je 955 PDF, 479 XLS i 228 DOCX resursa, a to su podaci koje nitko ne može spojiti ni analizirati.
- **Feature:** lokalni planovi nabave (208 skupova) i proračuni izvuku se u jedinstvenu shemu (CPV, iznos, godina) s oznakom pouzdanosti. Hrani ideju 13.
- **AI/DS:** ekstrakcija tablica, usklađivanje sheme (embeddings), evaluacija na ~50 ručno označenih tablica.
- **Rizici:** nitko nije pregledao uzorak (kvaliteta nepotvrđena), tuđe licence, pravilo open-source traži open-weights modele (hardver nepoznat).

### 16. "Tko koga osniva": graf javnog sektora (C ili A), rezerva
- **Feature:** JPT API ima 4668 tijela, 3888 s osnivačima (OIB). Interaktivni graf: klik na ministarstvo ili grad pokaže ustanove, poduzeća i (kroz EOJN) koliko ugovora imaju. Pokriva "transparentnost rada javnog sektora".
- **Za:** pravi API, vizualno upečatljiv (UI/UX + video).
- **Protiv:** plića AI (graf analitika, usklađivanje entiteta), nema jasnog problema koji rješava. EOJN pokriva 55% vrijednosti (bolnice i HEP se ne spajaju). Možda već postoji sličan alat (nepotvrđeno).

## Geminijevih 10 ideja (2026-10-23), moja kritička ocjena
_Izvor: Gemini (vanjski research na zahtjev korisnika, korisniku su draže od ideja 13-16). Provjera: moje CKAN pretrage 2026-10-23. VAŽNO: nula pogodaka u pretrazi nije dokaz da skup ne postoji na portalu, samo da ga nisam našao (nepotvrđeno). Gemini je sam označio nekoliko podataka kao nepotvrđene, a za neke tvrdnje stoji "[dokaz: ankete HUP-a]" bez linka._

- **19 Nabava.AI (C), SLABO:** Sudreg API vraća 401 (scout C), a bez njega nema osnivanja tvrtke ni vlasnika. EOJN izvoz nema broj ponuda, pa "samo jedan ponuditelj" nije izvedivo. GNN bez označenih primjera se ne može validirati. Označavanje ugovora i veza lokalnih čelnika ima pravni i GDPR rizik. To je ista ideja kao odbačena 17.
- **20 Vrtićko Oko (A), JAK PROBLEM, SLABI PODACI:** zagrebački vrtići imaju 320 točaka, ali bez kapaciteta i upisa (scout A). Podatak o živorođenima po naselju nisam našao (pretraga "zivorodeni" dala je samo DZS XLS stanovništva). Podataka o novogradnji nema. Predviđanje do 2029. s klizačem bilo bi izmišljeno i nevalidirano.
- **21 GlasGrađana (A), DEMO JAK, PODACI SLABI:** skup e-savjetovanja ima samo naslov, tijelo, status i datume (scout A), bez teksta nacrta i komentara. Sve ostalo bi bio scraping, što nije otvoreni podatak (kriterij 1 i 2 padaju). Ovisnost o plaćenom LLM API-ju (pravilo o otvorenom kodu, trošak, pad pri demu). Sažimanje zakona LLM-om je najočitija ideja, pa je vjerojatno zauzeta (moja procjena).
- **22 Birokracija ROI (C), ODBACITI:** Gemini sam priznaje da popis procedura nije strojno čitljiv (moja pretraga "procedur" našla je samo veterinarske procedure u HTML-u). "Process mining" traži zapise događaja kojih nema. Monte Carlo nad izmišljenim procjenama daje izmišljene brojke ("ušteda 2,5 mil. EUR").
- **23 PristupačniGrad (A), ISTO KAO 14:** isti problem i ista slabost. Visine rubnjaka i rampi nemamo, OSM oznake su rijetke (pokrivenost nepotvrđena). Demo "Google Maps vodi preko stepenica" je nepotvrđena tvrdnja. Dijkstra nije AI.
- **24 SubvencijaMatch (C), ODBACITI:** natječaje nisam našao na portalu (pretrage "hamag", "poziv projektni prijedlozi" = 0). Finin RGFI traži registraciju, Sudreg 401. Gemini sam predlaže "generirane financije" u demu, što uništava kriterij 1. Statistika "50% poduzetnika" nema izvor.
- **25 KrizniŠtab OS (A), NAJJAČI DEMO, PODACI DJELOMIČNI:** izazov A doslovno nabraja "javne usluge u kriznim situacijama". Nađeno: Hrvatske vode "područja sa potencijalno značajnim rizicima od poplava" (XML + WMS, sadržaj nepotvrđen), Adresar ustanova socijalne skrbi (CSV, Ministarstvo), zagrebački domovi za starije (85, s kapacitetom). Problemi: "simulacija vodostaja u Karlovcu" je u stvari crtanje poligona, a ne simulacija (nema podataka o vodostajima). "Točan broj štićenika" nema (kapacitet nije popunjenost). DHMZ upozorenja vraćaju 403 (scout A). AI/DS komponenta je prostorni join, plitko.
- **26 DokumentiBezReda (A), ODBACITI:** MUP redomata na portalu nisam našao (pretraga "redomat" = 0). Scraping bi dao tek par dana povijesti, a Prophet treba tjedne ili mjesece. Scraping MUP sustava nije otvoreni podatak i ima pravni i politički rizik.
- **27 DoktorGdje (A), NAJBOLJI PROBLEM, PODACI NEPOZNATI:** konkretna odluka za građanina, jednostavno sučelje, stvarni AI/NLP dio (usklađivanje naziva pretraga embeddingima, korisno za diplomski). Ali listu čekanja nisam našao na portalu (pretrage "cekanja", "liste cekanja" = 0). HZZO na portalu ima ugovorene sadržaje bolničke zaštite, DTS i lokacije ordinacija, ne vrijeme čekanja. Gemini sam piše "postojao je skup, ili JSON s hzzo.hr", bez potvrde. "Multi-objective optimization" je zapravo sortiranje. Postojeći pretraživač lista čekanja (Gemini ga navodi, ja ga nisam provjerio) već pokriva dio ideje.
- **28 LokalniProračun Usporedilica (C), IZVEDIVO, PLITKO:** pronašao sam skup "lokalni-prora-un-2001" (Min. financija, HTML + XLS), ostalo je kod općina u PDF/DOCX. Je li cijeli niz strojno čitljiv, nepotvrđeno. Indeks razvijenosti nisam našao. K-Means je plitak. Sličan "Proračun u malom" već postoji (IJF, po Geminiju, nepotvrđeno).

**Top 3 po meni:** 27 (ako postoje podaci), 25 (ako su poplavne zone upotrebljive), 20 (samo ako se nađu demografski podaci po četvrti). Geminijev top 3 bio je 19, 25, 27. Ne slažem se oko 19.

## 29 "Obrtnik.AI" (Gemini, 2026-10-24): osobni pravni štit i skener prilika za male poduzetnike (C), PITCH JAK, PODACI VEĆINOM NEPOSTOJEĆI NA PORTALU
- **Ideja:** upis OIB-a povuče NKD i lokaciju. Modul "Pravni štit" (zakoni iz Narodnih novina i e-Savjetovanja filtrirani po djelatnosti, RAG, to-do liste s rokovima) i modul "Skener prilika" (EOJN natječaji i eFondovi, semantički match s profilom tvrtke).
- **Dobro:** jasna persona (frizer, mehaničar, IT freelancer), stvarna bol, odličan video (hook, "Prevedi na ljudski", match score). Okvir problema vrijedi zadržati. Spaja moje ideje 13, 21 i 24.
- **Provjera izvora na CKAN-u (2026-10-24, nula pogodaka nije dokaz da ne postoji):**
  - Sudski/Obrtni registar po OIB-u: Sudreg API vraća 401 (scout C). Obrtni registar na portalu = HTML jedne županije. **One-click onboarding po OIB-u ne radi.**
  - Narodne novine: nema skupa na data.gov.hr, ALI NN ima vlastiti službeni API (provjereno 2026-10-24, vidi `context/data.md`): metapodaci propisa preko API-ja, puni tekst samo kao HTML po predvidivom URL-u (scraping). Licenca za ponovnu uporabu nepotvrđena.
  - e-Savjetovanja: postoji (CSV/JSON/XML), ali samo naslov, tijelo, status i datumi, bez teksta nacrta (scout A).
  - EOJN OCDS: 0 pogodaka. Imamo samo registar ugovora (već sklopljeni ugovori, 20 000 redaka do 05/2026), a **ne otvorene natječaje s rokom**. "Rok za prijavu: 4 dana" se ne može napraviti sa stvarnim podacima.
  - eFondovi / natječaji: 0 pogodaka na portalu.
  - Ostaje stvarno: NKD klasifikacija (JSON), e-savjetovanja metapodaci, registar ugovora.
- **Problemi:** (1) demo bi trebao izmišljene podatke, što udara kriterije 1 i 2 i Izvedivost; (2) pravni savjet LLM-om: halucinacija = pogrešna obveza ili propuštena kazna, a žiri je ministarstvo; nema skupa za evaluaciju; (3) preskočeno je glavno: zakoni rijetko navode NKD, pa je mapiranje zakon→djelatnost pravi problem; (4) OpenAI/Pinecone u suprotnosti s pravilom o softveru otvorenog koda (Mistral open-weights ok, hardver nepoznat); (5) opseg: 2 modula, 5 izvora, 4 osobe, 17 dana; (6) "150.000 poduzetnika" i konkurenti (IUS-INFO, Briefing.hr) bez izvora; (7) OIB obrtnika je osobni podatak (GDPR) u demu.
- **Verdikt:** ne graditi kako je napisano (Skener prilika i OIB onboarding otpadaju). **"Pravni štit" je sada izvediv kao samostalna ideja:** NN API daje popis propisa i metapodatke (naslov, vrsta, institucija, EuroVoc, pravno područje), a tekst se povlači iz HTML-a. Djelatnost bi korisnik birao iz liste umjesto OIB-a, točnost bi se mjerila na ~50 ručno označenih propisa. Preostali rizici: licenca za ponovnu uporabu teksta, mapiranje propis→djelatnost (NN ne daje NKD), halucinacije LLM-a, runtime model mora biti open-source. Ostaje otvoreno: ima li smisla kao samostalan projekt bez Skenera prilika.

## Odbačene / odgođene
_Jedan redak po ideji, da ih ne raspravljamo ponovno._

- **1 "Spojnik"** (meta-alat za otkrivanje i spajanje skupova, C): apstraktan, korisnik ga nije razumio, utjecaj neizravan (korisnici su analitičari), a spojevi po općini/županiji su trivijalni. Zamijenjen konkretnijim idejama 13-16. Nalazi o kvaliteti portala (npr. umjetničke organizacije 1177 zapisa u izvoru naspram 250 u datastoreu) ostaju u `context/scout/`.
- **2 "Profil četvrti" Zagreb** (A): nadograđen i zamijenjen idejom 14.
- **3 "Pomoć u blizini"** (udruge po ciljnoj skupini, A): u biti imenik, plitka inovativnost, traži geokodiranje slobodnog teksta adrese.
- **5 "Lanac javnog novca"** (EOJN x JPT, C): zasićena tema, izvoz odsječen, ključni izvori (Fina RGFI, Sudreg) zatvoreni. Može biti demo-primjer unutar 13 ili 16.
- **6 "Odblokiraj podatke"**: duplikat, spojen u 15.
- **7 "Zdravlje portala"**: podskup Spojnika, nizak utjecaj za građane, plitka AI.
- **8 "Tko koga osniva"**: duplikat, spojen u 16.
- **10 "Što je novo u mom kraju"** (A): plitka vrijednost, e-savjetovanja nemaju temu ni link.
- **11 "Karta kulturne praznine"** (A): gruba mjera (sjedište organizacije nije mjesto djelovanja), premalo za cijeli projekt. Može biti jedan sloj u drugoj ideji.
- **12 "Pitaj portal"** (chat nad podacima): samostalno slabo. Može biti sučelje nad 13 ili 15.
- **17 "Crveni alarmi u nabavi"** (C): nema označenih primjera da se dokaže da radi, označavanje ugovora kao sumnjivih ima pravni i ugledni rizik, zasićena tema.
- **18 "Geokoder za otvorene podatke RH"** (A): riješen problem (Nominatim), samostalno slab za demo. Modul ako se 14 proširi izvan Zagreba.
- **Izazov B** (odluka tima 2026-10-23): 4 lokalizirani model zraka, 9 "Hoću li danas na bicikl", plaže x DHMZ (ocjene iz 2015., izvorni CSV 404), poplave i požari (nema strojno čitljivog izvora).
- **Autotrolej uživo pozicije** (A): API traži token, pristup nepotvrđen.
