# Dnevnik

## 2026-10-22
- Dogovoren način rada: modularni md fileovi, asistent kritički savjetuje kao tech poduzetnik/softver inženjer. Postavljen kostur foldera.
- Primljen pozivni mail: Datathon Portala otvorenih podataka 2026 (MPUDT). Zapisano u `context/competition.md` i `context/timeline.md`. Uočeno: rok za prijavu je 23.10. u 24:00 (sutra).
- Folder je premješten u `Desktop/other/datathon` (klon repa github.com/SimeJadrijev/datathon). Prvi commit s md fileovima je napravljen i pushan na `origin/main` na izričit zahtjev korisnika. Vidljivost repa NIJE provjerena (`gh` nije instaliran).
- Pročitani Javni poziv i Pravila (PDF-ovi premješteni u `context/izvori/`). `competition.md`, `timeline.md`, `tasks.md` i `decisions.md` prepisani.
- **Ispravak:** prijavu ne podnosi samo mentor, nego svaki član tima ispunjava obrazac. Kriteriji: max 60 b, 9 kriterija, težište na izvedivosti, UI/UX-u i videu. Video do 3 min, obavezan, bez njega se rješenje ne ocjenjuje.
- Korisnik dodao skill `skills/product-brainstorming/SKILL.MD` (generički PM skill: modovi, HMW, JTBD, assumption testing). Nije u `.claude/skills/`, pa nije aktivan. Razgovor o tome koje još skillove dodati u fazi brainstorminga (prijedlog: custom skill za istraživanje data.gov.hr kataloga + custom "žiri" skill s rubrikom od 60 bodova).
- Skill premješten u `.claude/skills/product-brainstorming/SKILL.md` (aktivan). Napisana još 2 skilla: `jury-simulator` i `data-scout`. Provjereno curlom da data.gov.hr CKAN API radi na `/ckan/api/3/action/` (3956 skupova), upisano u `context/data.md`. Ključan nalaz: samo ~55 skupova se osvježava dnevno/tjedno/kontinuirano.
- Prijedlog (još ne odlučeno): ideja mora biti zaključana oko 27.10. da ostane ~10 dana za izradu. Brainstorm iz dva smjera (od podataka i od problema).
- Nepushani/necommitani: PDF-ovi u `context/izvori/`, `.claude/skills/` i izmjene md fileova nakon prvog commita.
- Commit `431ad70` pushan (pravila, skillovi, PDF-ovi). Pokrenuta 3 paralelna data-scouta (A, B, C), rezultati u `context/scout/izazov-*.md`, sažetak u `context/data.md`. Ključno: nema živih okolišnih mjerenja na portalu, geo podaci su gotovo samo Zagreb/Rijeka, C je zasićen. Preporuka izazova upisana u `work/decisions.md` (A ili B, ne C), čeka tim. Nalazi agenata nisu neovisno provjereni. U `/tmp/scout_scripts/` i `/tmp/tmp.DM15WB2E58` ostali privremeni file-ovi scouta (izvan repoa).
- Ispravljeno u `decisions.md`: tvrdnja da prijava ne obvezuje na predaju nije u Pravilima (nepotvrđeno).
- Korisnik (Sime) dao podatke o timu: Sime (data science, AI), Ivan (baze), Luka (DevOps), Nino (full-stack), svi frontend+backend. Upisano u `team/team.md`. Tim razmišlja o prijavi u više izazova.
- Pročitani cijeli scout fileovi, napisane 5 ideja u `work/ideas.md`. Revidirana preporuka (vidi `work/decisions.md`): glavni kandidat "Spojnik" (alat za otkrivanje/spajanje/zdravlje skupova, izazov C), rezerva "Profil četvrti Zagreb" (A). Raniju preporuku "ne C" ispravio sam (vrijedila je samo za dashboard javne nabave). Bodovi su moje grube procjene, `jury-simulator` nije formalno pokrenut. Skillovi iz `.claude/skills/` nisu učitani u ovoj sesiji (nema ih u `/context`), treba restart.
- Korisnik nije razumio ideju Spojnik i traži više ideja. Dodano pravilo u CLAUDE.md: na jednostavna pitanja odgovaraj kratko. Drugi krug ideja (6–12) upisan u `work/ideas.md`: AI za PDF/XLS tablice, zdravlje portala, graf javnog sektora, bicikl odluka Zagreb, feed iz dnevnih registara, karta kulturne praznine, chat nad podacima.
- Skillovi `video-script` (3 min) i UI/UX nisu napisani, treba ih tek u fazi izrade.

## 2026-10-24
- Dodani skillovi za fazu izrade: `.claude/skills/spring-boot-api` (slojevi, DTO recordi, jedinstveni oblik greške `{statusCode, message, error}` usklađen s frontendom, klijenti za vanjske API-je, testovi) i `.claude/skills/react-web` (struktura s barrelima, API/Query hookovi, wouter rute, UI/UX pravila za 4 stanja ekrana). Grana `chore/coding-skills`. Uočeno: `vite.config.ts` još nema proxy `/api` → `localhost:8080`.
- Skillovi `spring-boot-api` i `react-web` prevedeni na engleski; odluka o jeziku u `work/decisions.md`.
- Ispravak: sav kod na engleskom (ne hrvatske domenske imenice). Skillovi ažurirani, dodan rječnik domenskih pojmova HR → EN u `spring-boot-api`.
