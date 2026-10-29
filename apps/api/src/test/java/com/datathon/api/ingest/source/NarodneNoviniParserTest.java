package com.datathon.api.ingest.source;

import static com.datathon.api.ingest.source.SourceFixtures.text;
import static org.assertj.core.api.Assertions.assertThat;

import com.datathon.api.fundingcall.Area;
import com.datathon.api.ingest.http.PoliteHttpClient;
import com.datathon.api.ingest.http.RobotsRules;
import com.datathon.api.ingest.source.NarodneNoviniParser.EliMetadata;
import com.datathon.api.regulation.RegulationType;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NarodneNoviniParserTest {

    private List<ParsedRegulation> regulations;

    @BeforeEach
    void parseFixture() {
        regulations = NarodneNoviniParser.parseListPage(text("narodne-novine/list.html"));
    }

    @Test
    void parsesListPage() {
        assertThat(regulations).hasSize(20);
        ParsedRegulation law = regulations.getFirst();

        assertThat(law.externalId()).isEqualTo("2026-123-1465");
        assertThat(law.title()).isEqualTo("Zakon o izmjenama i dopunama Zakona o tržištu kapitala");
        assertThat(law.type()).isEqualTo(RegulationType.LAW);
        assertThat(law.gazetteNumber()).isEqualTo("123/2026");
        assertThat(law.documentNumber()).isEqualTo(1465);
        assertThat(law.publishedAt()).isEqualTo(LocalDate.of(2026, 10, 28));
        assertThat(law.url()).isEqualTo("https://narodne-novine.nn.hr/clanci/sluzbeni/2026_10_123_1465.html");
        assertThat(law.eliUrl()).isEqualTo("https://narodne-novine.nn.hr/eli/sluzbeni/2026/123/1465");
        assertThat(law.pdfUrl()).isEqualTo("https://narodne-novine.nn.hr/eli/sluzbeni/2026/123/1465/pdf");
        assertThat(law.eliMetadata()).isFalse();
    }

    @Test
    void hiddenRelevanceScoreIsInNoTitle() {
        assertThat(regulations).extracting(ParsedRegulation::title)
                .allSatisfy(title -> assertThat(title).doesNotEndWith("0").doesNotMatch(".*\\s\\d$"));
    }

    @Test
    void mapsActTypes() {
        assertThat(NarodneNoviniParser.typeOf("zakon")).isEqualTo(RegulationType.LAW);
        assertThat(NarodneNoviniParser.typeOf("uredba")).isEqualTo(RegulationType.DECREE);
        assertThat(NarodneNoviniParser.typeOf("pravilnik")).isEqualTo(RegulationType.ORDINANCE);
        assertThat(NarodneNoviniParser.typeOf("odluka")).isEqualTo(RegulationType.DECISION);
        assertThat(NarodneNoviniParser.typeOf("rješenje")).isEqualTo(RegulationType.OTHER);
        assertThat(NarodneNoviniParser.typeOf("presuda")).isEqualTo(RegulationType.OTHER);
        assertThat(regulations).extracting(ParsedRegulation::type).contains(
                RegulationType.LAW, RegulationType.ORDINANCE, RegulationType.DECISION, RegulationType.OTHER);
    }

    @Test
    void identifiersAreUniqueAndUseYearGazetteAndDocument() {
        assertThat(regulations).extracting(ParsedRegulation::externalId).doesNotHaveDuplicates()
                .allSatisfy(id -> assertThat(id).matches("\\d{4}-\\d+-\\d+"));
    }

    @Test
    void firstGuessOfAreasComesFromTheTitle() {
        ParsedRegulation capitalMarketLaw = regulations.getFirst();
        ParsedRegulation energyAct = NarodneNoviniParser.parseListPage(listItem(
                "Pravilnik o energetskoj učinkovitosti zgrada", "pravilnik")).getFirst();

        assertThat(energyAct.areas()).contains(Area.ENERGY);
        assertThat(capitalMarketLaw.areas()).doesNotContain(Area.ENERGY);
    }

    @Test
    void readsEliMetadataFromTheActPage() {
        EliMetadata eli = NarodneNoviniParser.parseEli(text("narodne-novine/act-law.html")).orElseThrow();

        assertThat(eli.enactedAt()).isEqualTo(LocalDate.of(2026, 10, 21));
        assertThat(eli.publishedAt()).isEqualTo(LocalDate.of(2026, 10, 28));
        assertThat(eli.type()).isEqualTo(RegulationType.LAW);
    }

    @Test
    void pageWithoutEliMetadataGivesNothing() {
        assertThat(NarodneNoviniParser.parseEli("<html><head></head><body>Nema ELI</body></html>")).isEmpty();
    }

    @Test
    void robotsTxtBlocksTheAdsSectionButNotTheOfficialList() {
        RobotsRules rules = RobotsRules.parse(text("narodne-novine/robots.txt"), PoliteHttpClient.USER_AGENT);

        assertThat(rules.isAllowed("/eli/oglasni/2024/111/pdf")).isFalse();
        assertThat(rules.isAllowed("/search.aspx?kategorija=3&godina=2026")).isFalse();
        assertThat(rules.isAllowed("/search.aspx?sortiraj=4&kategorija=1&godina=2026&rpp=200&qtype=1&pretraga=da&str=0"))
                .isTrue();
        assertThat(rules.isAllowed("/clanci/sluzbeni/2026_10_123_1465.html")).isTrue();
    }

    private static String listItem(String title, String type) {
        return """
                <div class="searchListItem"><table><tr><td>
                <div class="resultTitle"><a href="/clanci/sluzbeni/2026_10_1_1.html">%s <span class="score" style="display:none;">0</span></a></div>
                <div class="official-number-and-date">NN 1/2026, (1),   %s, 2.1.2026.</div>
                </td></tr></table></div>
                """.formatted(title, type);
    }
}
