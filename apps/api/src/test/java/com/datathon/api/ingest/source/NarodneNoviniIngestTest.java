package com.datathon.api.ingest.source;

import static com.datathon.api.ingest.source.SourceFixtures.text;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.datathon.api.ingest.RegulationIngest.Outcome;
import com.datathon.api.ingest.http.HttpFetchException;
import com.datathon.api.ingest.http.PoliteHttpClient;
import com.datathon.api.regulation.RegulationType;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class NarodneNoviniIngestTest {

    private static final String LIST_2026 = "https://narodne-novine.nn.hr/search.aspx?sortiraj=4&kategorija=1&godina=2026"
            + "&rpp=200&qtype=1&pretraga=da&str=0";
    private static final String LIST_2025 = LIST_2026.replace("godina=2026", "godina=2025");
    private static final String LAW_PAGE = "https://narodne-novine.nn.hr/clanci/sluzbeni/2026_10_123_1465.html";

    private final PoliteHttpClient http = mock(PoliteHttpClient.class);
    private final RegulationUpserter upserter = mock(RegulationUpserter.class);
    private NarodneNoviniIngest ingest;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-10-29T10:00:00Z"), ZoneOffset.UTC);
        ingest = new NarodneNoviniIngest(http, upserter, clock);
        when(http.getText(anyString())).thenReturn("");
    }

    @Test
    void readsCurrentAndPreviousYearAndReportsHowManyActsWereSaved() {
        when(http.getText(LIST_2026)).thenReturn(text("narodne-novine/list.html"));
        when(http.getText(LIST_2025)).thenReturn(text("narodne-novine/list.html").replace("/2026,", "/2025,"));

        Outcome outcome = ingest.ingest();

        assertThat(outcome.fetched()).isEqualTo(40);
        assertThat(outcome.errors()).isZero();
        verify(http).getText(LIST_2025);
    }

    @Test
    void olderPagesAreOnlyReadWhileAPageIsFull() {
        when(http.getText(LIST_2026)).thenReturn(text("narodne-novine/list.html"));

        ingest.ingest();

        verify(http, never()).getText(LIST_2026.replace("str=0", "str=1"));
    }

    @Test
    void newestLawGetsEliMetadataAndTheFlag() {
        when(http.getText(LIST_2026)).thenReturn(text("narodne-novine/list.html"));
        when(http.getText(LAW_PAGE)).thenReturn(text("narodne-novine/act-law.html"));

        ingest.ingest();

        ParsedRegulation enriched = savedWithEli().getFirst();
        assertThat(enriched.eliMetadata()).isTrue();
        assertThat(enriched.type()).isEqualTo(RegulationType.LAW);
        assertThat(enriched.enactedAt()).hasToString("2026-10-21");
    }

    @Test
    void failingEliPageDoesNotStopTheRun() {
        when(http.getText(LIST_2026)).thenReturn(text("narodne-novine/list.html"));
        when(http.getText(LAW_PAGE)).thenThrow(new HttpFetchException("down"));

        Outcome outcome = ingest.ingest();

        assertThat(outcome.fetched()).isEqualTo(20);
        assertThat(outcome.errors()).isZero();
        assertThat(savedWithEli()).isEmpty();
    }

    @Test
    void failingYearIsCountedAsErrorWhileTheOtherYearIsKept() {
        when(http.getText(LIST_2026)).thenReturn(text("narodne-novine/list.html"));
        when(http.getText(LIST_2025)).thenThrow(new HttpFetchException("HTTP 500"));

        Outcome outcome = ingest.ingest();

        assertThat(outcome.fetched()).isEqualTo(20);
        assertThat(outcome.errors()).isEqualTo(1);
        assertThat(outcome.errorMessage()).contains("2025");
    }

    @Test
    void onlyTheNewestSixtyLawsAreReadForEli() {
        String lawsOnly = lawPage(80);
        when(http.getText(LIST_2026)).thenReturn(lawsOnly);

        ingest.ingest();

        ArgumentCaptor<String> pages = ArgumentCaptor.forClass(String.class);
        verify(http, atLeastOnce()).getText(pages.capture());
        long actPagesRead = pages.getAllValues().stream().filter(url -> url.contains("/clanci/sluzbeni/")).count();
        assertThat(actPagesRead).isEqualTo(NarodneNoviniIngest.NEWEST_LAWS_WITH_ELI);
    }

    private List<ParsedRegulation> savedWithEli() {
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ParsedRegulation>> saved = ArgumentCaptor.forClass(List.class);
        verify(upserter, atLeastOnce()).upsertAll(saved.capture());
        List<ParsedRegulation> withEli = new ArrayList<>();
        saved.getAllValues().forEach(batch -> batch.stream().filter(ParsedRegulation::eliMetadata).forEach(withEli::add));
        return withEli;
    }

    private static String lawPage(int count) {
        StringBuilder html = new StringBuilder("<html><body>");
        for (int document = 1; document <= count; document++) {
            html.append("""
                    <div class="searchListItem"><table><tr><td>
                    <div class="resultTitle"><a href="/clanci/sluzbeni/2026_10_1_%1$d.html">Zakon broj %1$d</a></div>
                    <div class="official-number-and-date">NN 1/2026, (%1$d),   zakon, 2.1.2026.</div>
                    </td></tr></table></div>
                    """.formatted(document));
        }
        return html.append("</body></html>").toString();
    }
}
