package com.datathon.api.meta;

import static org.assertj.core.api.Assertions.assertThat;

import com.datathon.api.fundingcall.FundingCall;
import com.datathon.api.fundingcall.FundingCallRepository;
import com.datathon.api.fundingcall.FundingCallStatus;
import com.datathon.api.ingest.IngestRun;
import com.datathon.api.ingest.IngestRunRepository;
import com.datathon.api.ingest.SourceRunLog;
import com.datathon.api.ingest.SourceRunLogRepository;
import com.datathon.api.regulation.Regulation;
import com.datathon.api.regulation.RegulationRepository;
import com.datathon.api.source.SourceCatalog;
import com.datathon.api.source.SourceCode;
import com.datathon.api.source.SourceService;
import com.datathon.api.source.SourceSummary;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import tools.jackson.databind.json.JsonMapper;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MetaAndSourceServiceTest {

    private static final Instant FIRST_RUN = Instant.parse("2026-10-27T08:00:00Z");
    private static final Instant SECOND_RUN = Instant.parse("2026-10-28T08:00:00Z");

    @Autowired
    private FundingCallRepository fundingCallRepository;

    @Autowired
    private RegulationRepository regulationRepository;

    @Autowired
    private IngestRunRepository ingestRunRepository;

    @Autowired
    private SourceRunLogRepository sourceRunLogRepository;

    @Autowired
    private EntityManager entityManager;

    private MetaService metaService;
    private SourceService sourceService;
    private SnapshotDate snapshotDate;

    @BeforeEach
    void setUp() {
        entityManager.createNativeQuery("TRUNCATE TABLE funding_call, regulation, ingest_run CASCADE").executeUpdate();
        JsonMapper jsonMapper = JsonMapper.builder().build();
        snapshotDate = new SnapshotDate(jsonMapper);
        metaService = new MetaService(snapshotDate, ingestRunRepository, fundingCallRepository, regulationRepository);
        sourceService = new SourceService(new SourceCatalog(jsonMapper), fundingCallRepository, regulationRepository,
                sourceRunLogRepository, snapshotDate);
    }

    @Test
    void metaCountsOnlyVisibleOpenCallsAndAllRegulations() {
        saveCall(SourceCode.HZZ, "a", FundingCallStatus.OPEN, false);
        saveCall(SourceCode.HZZ, "b", FundingCallStatus.CLOSED, false);
        saveCall(SourceCode.HZZ, "c", FundingCallStatus.OPEN, true);
        regulationRepository.save(new Regulation("nn-1", "Zakon"));

        var meta = metaService.get();

        assertThat(meta.counts().openFundingCalls()).isEqualTo(1);
        assertThat(meta.counts().regulations()).isEqualTo(1);
        assertThat(meta.snapshotDate()).isEqualTo(snapshotDate.value().orElseThrow());
    }

    @Test
    void lastIngestIsNullWithoutFinishedRunsAndOtherwiseTheLatestFinish() {
        assertThat(metaService.get().lastIngestAt()).isNull();

        ingestRunRepository.save(finishedRun(FIRST_RUN));
        ingestRunRepository.save(finishedRun(SECOND_RUN));
        ingestRunRepository.save(new IngestRun(Instant.parse("2026-10-29T08:00:00Z"), null));

        assertThat(metaService.get().lastIngestAt()).isEqualTo(SECOND_RUN.plusSeconds(60));
    }

    @Test
    void sourcesListEverySourceWithRecordCountsAndNullForSudreg() {
        saveCall(SourceCode.HZZ, "a", FundingCallStatus.OPEN, false);
        saveCall(SourceCode.HZZ, "b", FundingCallStatus.CLOSED, false);
        regulationRepository.save(new Regulation("nn-1", "Zakon"));

        var sources = sourceService.list();

        assertThat(sources).extracting(SourceSummary::code).containsExactlyInAnyOrder(SourceCode.values());
        assertThat(recordCountOf(sources, SourceCode.HZZ)).isEqualTo(2L);
        assertThat(recordCountOf(sources, SourceCode.NARODNE_NOVINE)).isEqualTo(1L);
        assertThat(recordCountOf(sources, SourceCode.FZOEU)).isZero();
        assertThat(recordCountOf(sources, SourceCode.SUDREG)).isNull();
    }

    @Test
    void lastSuccessfulFetchFallsBackToTheSnapshotDateAndIgnoresFailedRuns() {
        IngestRun run = ingestRunRepository.save(finishedRun(FIRST_RUN));
        sourceRunLogRepository.save(runLog(run, SourceCode.HZZ, FIRST_RUN, 0));
        sourceRunLogRepository.save(runLog(run, SourceCode.HZZ, SECOND_RUN, 2));
        sourceRunLogRepository.save(runLog(run, SourceCode.MZOM, SECOND_RUN, 1));

        var sources = sourceService.list();

        assertThat(lastFetchOf(sources, SourceCode.HZZ)).isEqualTo(FIRST_RUN);
        Instant snapshotInstant = LocalDate.of(2026, 10, 26).atStartOfDay(ZoneOffset.UTC).toInstant();
        assertThat(lastFetchOf(sources, SourceCode.MZOM)).isEqualTo(snapshotInstant);
        assertThat(lastFetchOf(sources, SourceCode.FZOEU)).isEqualTo(snapshotInstant);
    }

    private void saveCall(SourceCode source, String externalId, FundingCallStatus status, boolean hidden) {
        var call = new FundingCall(source, externalId, "Poziv " + externalId);
        call.setStatus(status);
        call.setHidden(hidden);
        fundingCallRepository.save(call);
    }

    private static IngestRun finishedRun(Instant startedAt) {
        var run = new IngestRun(startedAt, null);
        run.setFinishedAt(startedAt.plusSeconds(60));
        return run;
    }

    private static SourceRunLog runLog(IngestRun run, SourceCode source, Instant startedAt, int errors) {
        var log = new SourceRunLog(run, source, startedAt);
        log.setErrors(errors);
        return log;
    }

    private static Long recordCountOf(List<SourceSummary> sources, SourceCode code) {
        return sources.stream().filter(source -> source.code() == code).findFirst().orElseThrow().recordCount();
    }

    private static Instant lastFetchOf(List<SourceSummary> sources, SourceCode code) {
        return sources.stream().filter(source -> source.code() == code).findFirst().orElseThrow().lastSuccessfulFetch();
    }
}
