package com.datathon.api.ingest.source;

import static com.datathon.api.ingest.source.SourceFixtures.text;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.datathon.api.fundingcall.FundingCallRepository;
import com.datathon.api.fundingcall.LegalReference;
import com.datathon.api.ingest.RegulationIngest.Outcome;
import com.datathon.api.ingest.http.HttpFetchException;
import com.datathon.api.ingest.http.PoliteHttpClient;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class CitedRegulationIngestTest {

    private static final Clock NOVEMBER_2026 = Clock.fixed(Instant.parse("2026-11-03T10:00:00Z"), ZoneId.of("Europe/Zagreb"));

    private final PoliteHttpClient http = mock(PoliteHttpClient.class);
    private final RegulationUpserter upserter = mock(RegulationUpserter.class);
    private final FundingCallRepository fundingCallRepository = mock(FundingCallRepository.class);
    private final CitedRegulationIngest ingest =
            new CitedRegulationIngest(http, upserter, fundingCallRepository, NOVEMBER_2026);

    @Test
    void importsOnlyTheCitedActFromAnOlderIssue() {
        when(fundingCallRepository.findDistinctTitledLegalReferences()).thenReturn(List.of(
                new LegalReference("Zakona o Fondu za zaštitu okoliša i energetsku učinkovitost", "144/2012")));
        when(http.getText(CitedRegulationIngest.ISSUE_URL.formatted("2012", "144")))
                .thenReturn(text("narodne-novine/issue-144-2012.html"));

        Outcome outcome = ingest.ingest();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ParsedRegulation>> saved = ArgumentCaptor.forClass(List.class);
        verify(upserter).upsertAll(saved.capture());
        assertThat(saved.getValue()).extracting(ParsedRegulation::title)
                .containsExactly("Zakon o izmjenama i dopunama Zakona o Fondu za zaštitu okoliša i energetsku učinkovitost");
        assertThat(saved.getValue().getFirst().gazetteNumber()).isEqualTo("144/2012");
        assertThat(outcome.fetched()).isEqualTo(1);
    }

    @Test
    void issuesFromTheYearsTheMainIngestReadsAreSkipped() {
        when(fundingCallRepository.findDistinctTitledLegalReferences()).thenReturn(List.of(
                new LegalReference("Zakona o gradnji", "155/2025"),
                new LegalReference("Zakona o udrugama", "98/2026")));

        ingest.ingest();

        verify(http, never()).getText(anyString());
    }

    @Test
    void unreadableIssueIsCountedAndTheRestContinues() {
        when(fundingCallRepository.findDistinctTitledLegalReferences()).thenReturn(List.of(
                new LegalReference("Zakona o udrugama", "74/2014"),
                new LegalReference("Zakona o Fondu za zaštitu okoliša i energetsku učinkovitost", "144/2012")));
        when(http.getText(CitedRegulationIngest.ISSUE_URL.formatted("2014", "74")))
                .thenThrow(new HttpFetchException("HTTP 503", 503));
        when(http.getText(CitedRegulationIngest.ISSUE_URL.formatted("2012", "144")))
                .thenReturn(text("narodne-novine/issue-144-2012.html"));

        Outcome outcome = ingest.ingest();

        assertThat(outcome.errors()).isEqualTo(1);
        assertThat(outcome.fetched()).isEqualTo(1);
    }
}
