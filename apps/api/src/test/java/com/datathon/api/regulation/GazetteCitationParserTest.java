package com.datathon.api.regulation;

import static org.assertj.core.api.Assertions.assertThat;

import com.datathon.api.regulation.GazetteCitationParser.Citation;
import java.util.List;
import org.junit.jupiter.api.Test;

class GazetteCitationParserTest {

    @Test
    void readsLawWithQuotedGazetteAndTwoDigitYears() {
        List<Citation> citations = GazetteCitationParser.parse("Na temelju odredbi članka 7. Zakona o Fondu za zaštitu "
                + "okoliša i  energetsku učinkovitost („Narodne novine“ broj 107/03 i 144/12), članka 21.");

        assertThat(citations).containsExactly(
                new Citation("Zakona o Fondu za zaštitu okoliša i energetsku učinkovitost", "107/2003"),
                new Citation("Zakona o Fondu za zaštitu okoliša i energetsku učinkovitost", "144/2012"));
    }

    @Test
    void readsShortNnFormWithFourDigitYears() {
        List<Citation> citations = GazetteCitationParser.parse("sukladno Pravilniku o provedbi Programa potpore za "
                + "izostanak prihoda od svinjogojske proizvodnje (NN 25/2024, 90/2024) Agencija za plaćanja");

        assertThat(citations).extracting(Citation::gazetteNumber).containsExactly("25/2024", "90/2024");
        assertThat(citations.getFirst().citedTitle()).startsWith("Pravilniku o provedbi Programa potpore");
    }

    @Test
    void readsListWithTrailingDotsAndAnd() {
        List<Citation> citations = GazetteCitationParser.parse(
                "Sukladno članku 33. Zakona o udrugama (Narodne novine, broj 74/14., 70/17., 98/19. i 151/22.) te");

        assertThat(citations).extracting(Citation::gazetteNumber)
                .containsExactly("74/2014", "70/2017", "98/2019", "151/2022");
        assertThat(citations.getFirst().citedTitle()).isEqualTo("Zakona o udrugama");
    }

    @Test
    void stopsAtTheWordOdAndIgnoresClassificationCodes() {
        List<Citation> citations = GazetteCitationParser.parse("KLASA: 112-01/26-01/00044 objavljen u Narodnim "
                + "novinama br. 130/2025 od 17. listopada 2025. objavljuje 3/4 natječaja");

        assertThat(citations).containsExactly(new Citation(null, "130/2025"));
    }

    @Test
    void readsSeveralActsInOneSentence() {
        List<Citation> citations = GazetteCitationParser.parse("u skladu sa Zakonom o klimatskim promjenama i zaštiti "
                + "ozonskog sloja (»Narodne novine«, br. 67/25, u daljnjem tekstu: Zakon), Uredbom o mehanizmima "
                + "financiranja (NN 1/26)");

        assertThat(citations).containsExactly(
                new Citation("Zakonom o klimatskim promjenama i zaštiti ozonskog sloja", "67/2025"),
                new Citation("Uredbom o mehanizmima financiranja", "1/2026"));
    }

    @Test
    void oldTwoDigitYearIsLastCentury() {
        assertThat(GazetteCitationParser.parse("Zakon o predškolskom odgoju i obrazovanju (Narodne novine 10/97)"))
                .extracting(Citation::gazetteNumber).containsExactly("10/1997");
    }

    @Test
    void textWithoutGazetteGivesNothing() {
        assertThat(GazetteCitationParser.parse("Rok za prijavu je 15/30 dana od objave.")).isEmpty();
        assertThat(GazetteCitationParser.parse(null)).isEmpty();
    }

    @Test
    void citedTitleInAnotherCaseRefersToTheRegulationAndItsAmendments() {
        String cited = "Zakona o Fondu za zaštitu okoliša i energetsku učinkovitost";

        assertThat(GazetteCitationParser.refersTo(cited, "Zakon o Fondu za zaštitu okoliša i energetsku učinkovitost"))
                .isTrue();
        assertThat(GazetteCitationParser.refersTo(cited,
                "Zakon o izmjenama i dopunama Zakona o Fondu za zaštitu okoliša i energetsku učinkovitost")).isTrue();
        assertThat(GazetteCitationParser.refersTo(cited, "Zakon o zaštiti okoliša")).isFalse();
        assertThat(GazetteCitationParser.refersTo(null, "Zakon o zaštiti okoliša")).isFalse();
    }

    @Test
    void lawIsNotLinkedToADecisionAboutIt() {
        assertThat(GazetteCitationParser.refersTo("Zakonu o radu",
                "Odluka o nedavanju vjerodostojnog tumačenja članka 77. stavka 1. Zakona o radu")).isFalse();
        assertThat(GazetteCitationParser.refersTo("Zakonu o radu", "Zakon o izmjenama i dopunama Zakona o radu")).isTrue();
    }

    @Test
    void planWithoutTheWordOStillHasASubject() {
        assertThat(GazetteCitationParser.refersTo("Strateškog plana Zajedničke poljoprivredne politike Republike Hrvatske",
                "Strateški plan Zajedničke poljoprivredne politike Republike Hrvatske 2023. – 2027.")).isTrue();
    }

    @Test
    void issueUrlUsesEli() {
        assertThat(GazetteCitationParser.issueUrl("144/2012")).isEqualTo("https://narodne-novine.nn.hr/eli/sluzbeni/2012/144");
    }
}
