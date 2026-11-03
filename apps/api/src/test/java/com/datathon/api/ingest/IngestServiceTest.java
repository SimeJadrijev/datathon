package com.datathon.api.ingest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import com.datathon.api.fundingcall.FundingCall;
import com.datathon.api.fundingcall.FundingCallRepository;
import com.datathon.api.ingest.extract.FundingFilter;
import com.datathon.api.ingest.extract.RuleExtractor;
import com.datathon.api.source.SourceCode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.TestConfiguration;
import com.datathon.api.fundingcall.ExpiredFundingCallCloser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({IngestService.class, FundingFilter.class, RuleExtractor.class, FundingCallUpserter.class,
        ExpiredFundingCallCloser.class, IngestServiceTest.Fetchers.class})
class IngestServiceTest {

    @TestConfiguration
    static class Fetchers {

        @Bean
        SourceFetcher fzoeuFetcher() {
            return FakeSourceFetcher.returning(SourceCode.FZOEU,
                    item("ingest-test-1", "Javni poziv za sufinanciranje solarnih elektrana"),
                    item("ingest-test-2", "Javni poziv za energetsku obnovu obiteljskih kuća"),
                    item("ingest-test-3", "Oglas za prijam u službu"));
        }

        @Bean
        SourceFetcher failingFetcher() {
            return FakeSourceFetcher.failing(SourceCode.HZZ, new IllegalStateException("source is down"));
        }
    }

    @Autowired
    private IngestService ingestService;

    @Autowired
    private FundingCallRepository fundingCallRepository;

    @Autowired
    private SourceRunLogRepository sourceRunLogRepository;

    @Test
    void secondRunCreatesNoDuplicates() {
        long before = fundingCallRepository.count();

        IngestRun first = ingestService.runNow(Optional.of(SourceCode.FZOEU));
        IngestRun second = ingestService.runNow(Optional.of(SourceCode.FZOEU));

        assertThat(fundingCallRepository.count()).isEqualTo(before + 2);
        assertThat(sourceRunLogRepository.findByRunId(first.getId()))
                .extracting(SourceRunLog::getCreated, SourceRunLog::getUpdated).containsExactly(tuple(2, 0));
        assertThat(sourceRunLogRepository.findByRunId(second.getId()))
                .extracting(SourceRunLog::getCreated, SourceRunLog::getUpdated).containsExactly(tuple(0, 0));
        FundingCall saved = fundingCallRepository.findBySourceAndExternalId(SourceCode.FZOEU, "ingest-test-1").orElseThrow();
        assertThat(saved.getDeadline()).isEqualTo(LocalDate.of(2026, 11, 30));
        assertThat(saved.getRawTextHash()).hasSize(64);
        assertThat(saved.getFirstSeenAt()).isNotNull();
    }

    @Test
    void failingSourceDoesNotStopTheOthersAndIsLogged() {
        IngestRun run = ingestService.runNow(Optional.empty());

        List<SourceRunLog> logs = sourceRunLogRepository.findByRunId(run.getId());
        assertThat(logs).extracting(SourceRunLog::getSource)
                .containsExactlyInAnyOrder(SourceCode.FZOEU, SourceCode.HZZ);
        SourceRunLog failed = logs.stream().filter(log -> log.getSource() == SourceCode.HZZ).findFirst().orElseThrow();
        assertThat(failed.getErrors()).isEqualTo(1);
        assertThat(failed.getErrorMessage()).contains("source is down");
        SourceRunLog succeeded = logs.stream().filter(log -> log.getSource() == SourceCode.FZOEU).findFirst().orElseThrow();
        assertThat(succeeded.getFetched()).isEqualTo(2);
        assertThat(succeeded.getSkipped()).isEqualTo(1);
    }

    private static RawItem item(String externalId, String title) {
        return RawItem.builder()
                .externalId(externalId)
                .url("https://www.fzoeu.hr/test/" + externalId)
                .title(title)
                .provider("FZOEU")
                .publishedAt(LocalDate.of(2026, 10, 1))
                .text("Opis poziva za građane i kućanstva.")
                .build();
    }
}
