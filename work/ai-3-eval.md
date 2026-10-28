# AI-3: sažeci propisa, provjera

Uzorak: 10 najnovijih zakona iz seeda (NN 77/2026 do 114/2026), tekst dohvaćen s narodne-novine.nn.hr (`/clanci/sluzbeni/`, dopušteno u robots.txt), model claude-haiku-5-5. Trošak: 0,012 USD za svih 10 (oko 0,001 USD po zakonu).

Ručno provjereno u tekstu zakona (stupanje na snagu i iznosi):
- Zakon o obrtu (NN 114/2026-1370): stupa na snagu 1.1.2027. Točno.
- Zakon o potrošačkim kreditima (NN 86/2026-1071): 20.11.2026., čl. 114 st. 1-6 od 1.6.2027. Točno.
- Zakon o provedbi Uredbe 2024/1781 (NN 114/2026-1368): osmoga dana od objave, osim dijela čl. 30 za srednje poduzetnike od 19.7.2030. Točno.
- Zakon o izmjenama Prekršajnog zakona (NN 86/2026-1073): osmoga dana od objave, osim čl. 11 od 1.7.2027. Prva verzija je pogrešno napisala da tekst ne navodi stupanje na snagu.

Što je mjerenje otkrilo (ispravljeno):
- Slanje prvih 15.000 i zadnjih 3.000 znakova ispustilo je članak o stupanju na snagu kod zakona od oko 19.500 znakova. Sad se zakoni do 25.000 znakova šalju cijeli, a dulji dobivaju početak, izvadak oko "stupa na snagu" i kraj.
- Model je pisao "tekst ne navodi...". Prompt to zabranjuje.

Ostalo:
- Tvrdnje o kaznama (npr. 20.000 do 50.000 eura u zakonu o ekološkom dizajnu) i područjima (`areas`) nisu sve provjerene. Područja su gruba, jer neki zakoni ne pripadaju nijednom našem području.
- Sažetak je 2-3 duge rečenice, što je za karticu zakona pregusto. Skratiti prije predaje ako UI to traži.
