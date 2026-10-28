package com.datathon.api.ingest.extract;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class FundingFilterTest {

    private final FundingFilter filter = new FundingFilter();

    @ParameterizedTest
    @ValueSource(strings = {
        "VAUČERI ZA DIGITALIZACIJU - Vaučer za složena digitalna rješenja (VSD)",
        "Javni poziv za podnošenje prijava za dodjelu jednokratnih financijskih potpora udrugama na području grada Vrgorca za 2020. godinu",
        "Potpore za očuvanje radnih mjesta – skraćivanje radnog vremena",
        "Javni poziv za predlaganje javnih potreba u kulturi Općine Stupnik za 2027. godinu",
        "Javni natječaj za financiranje projekata udruga usmjerenih pružanju podrške provođenju programa postupanja/izvršavanja kazne zatvora",
        "Poziv na dodjelu bespovratnih sredstava C1.4. R3-I3 Izgradnja nove skele „Križnica“, Općina Pitomača",
        "JAVNI POZIV za financiranje jednokratnih programa i projekata sportskih udruga ŠZGBJ za 2022. godinu",
        "Javni poziv za financiranje programa/projekata razvoja lovstva na području grada Vrgorca za 2026. godinu",
        "Javni poziv za predlaganje programa i projekata od interesa za opće dobro koje provode udruge na području Općine Unešić za 2026. godinu",
        "Podnošenje zahtjeva za potporu za Mjeru 2. Potpora malim mljekarama za nadoknadu troškova sabiranja mlijeka za 2026. godinu od 1. do 31. svibnja 2026.",
        "Javni poziv za sufinanciranje nabave energetski učinkovitih vozila (EnU-2/26)",
        "Natječaj za dodjelu državnih stipendija u kategoriji izvrsnosti"
    })
    void acceptsFundingCalls(String title) {
        assertThat(filter.isFunding(title)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "Odluka o obustavi postupka Internog oglasa za popunjavanje radnog mjesta na neodređeno vrijeme",
        "Odluka o obustavi postupka Javnog natječaja za prijam u državnu službu na neodređeno vrijeme",
        "Javni natječaj za izbor na radno mjesto lektora hrvatskoga jezika i književnosti na visokim učilištima u inozemstvu za akademsku godinu 2026./2027.",
        "Odluka o poništenju postupka odabira individualnog konzultanta za praćenje i vrednovanje učinka eksperimentalnog programa Cjelodnevne škole",
        "Poziv na iskaz interesa za individualne konzultantske usluge u funkciji NADZORNOG INŽENJERA ZA SOFTVER ZA PRAĆENJE I VREDNOVANJE (M&E)",
        "Poziv na iskaz interesa za individualne konzultantske usluge u funkciji STRUČNJAKA ZA EVALUACIJU PROGRAMA",
        "Javna objava rješenja o prijamu u državnu službu",
        "Nabava intelektualnih usluga vanjskih stručnjaka za potrebe provedbe Projekta \"Hrvatska: Ususret održivom, pravednom i učinkovitom obrazovanju\"",
        "Nabava usluge izrade komunikacijske strategije i provedbe odnosa s javnošću za potrebe Projekta \"Hrvatska: Ususret održivom, pravednom i učinkovitom obrazovanju\"",
        "Natječaj za radno mjesto izvršnog ravnatelja Europskog centra za moderne jezike Vijeća Europe (ECML)",
        "Obavijest o razgovoru s ravnateljem škole vezano uz Javni poziv za izbor stručnog suradnika za rad u Europskoj školi"
    })
    void rejectsJobAdsProcurementAndNotices(String title) {
        assertThat(filter.isFunding(title)).isFalse();
    }
}
