package com.datathon.api.ingest;

import com.datathon.api.fundingcall.ExpiredFundingCallCloser;
import com.datathon.api.ingest.extract.FundingFilter;
import com.datathon.api.ingest.extract.RuleExtractor;
import com.datathon.api.source.SourceCode;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class IngestService {

    private static final int MAX_ERROR_MESSAGE_CHARS = 2000;

    private final List<SourceFetcher> fetchers;
    private final List<RegulationIngest> regulationIngests;
    private final FundingFilter fundingFilter;
    private final RuleExtractor ruleExtractor;
    private final FundingCallUpserter upserter;
    private final IngestRunRepository ingestRunRepository;
    private final SourceRunLogRepository sourceRunLogRepository;
    private final ExpiredFundingCallCloser expiredFundingCallCloser;

    public IngestRun createRun(Optional<SourceCode> source) {
        return ingestRunRepository.save(new IngestRun(Instant.now(), source.orElse(null)));
    }

    public void execute(Long runId) {
        IngestRun run = ingestRunRepository.findById(runId)
                .orElseThrow(() -> new IllegalArgumentException("Ingest run " + runId + " does not exist"));
        for (SourceFetcher fetcher : selectedFetchers(run.getRequestedSource())) {
            sourceRunLogRepository.save(runSource(run, fetcher));
        }
        for (RegulationIngest regulationIngest : selectedRegulationIngests(run.getRequestedSource())) {
            sourceRunLogRepository.save(runRegulations(run, regulationIngest));
        }
        expiredFundingCallCloser.closeExpired();
        run.setFinishedAt(Instant.now());
        ingestRunRepository.save(run);
    }

    public void discardRun(Long runId) {
        ingestRunRepository.deleteById(runId);
    }

    public IngestRun runNow(Optional<SourceCode> source) {
        IngestRun run = createRun(source);
        execute(run.getId());
        return run;
    }

    private List<SourceFetcher> selectedFetchers(SourceCode requested) {
        return fetchers.stream()
                .filter(fetcher -> requested == null || fetcher.code() == requested)
                .toList();
    }

    private List<RegulationIngest> selectedRegulationIngests(SourceCode requested) {
        return regulationIngests.stream()
                .filter(regulationIngest -> requested == null || regulationIngest.code() == requested)
                .toList();
    }

    private SourceRunLog runRegulations(IngestRun run, RegulationIngest regulationIngest) {
        long startNanos = System.nanoTime();
        SourceRunLog runLog = new SourceRunLog(run, regulationIngest.code(), Instant.now());
        try {
            RegulationIngest.Outcome outcome = regulationIngest.ingest();
            runLog.setFetched(outcome.fetched());
            runLog.setErrors(outcome.errors());
            runLog.setErrorMessage(outcome.errorMessage());
        } catch (RuntimeException e) {
            recordError(runLog, "regulations", e);
            log.error("{}: source failed", regulationIngest.code(), e);
        }
        runLog.setDurationMs((System.nanoTime() - startNanos) / 1_000_000);
        log.info("{}: {} fetched, {} errors in {} ms", regulationIngest.code(), runLog.getFetched(),
                runLog.getErrors(), runLog.getDurationMs());
        return runLog;
    }

    private SourceRunLog runSource(IngestRun run, SourceFetcher fetcher) {
        long startNanos = System.nanoTime();
        SourceRunLog runLog = new SourceRunLog(run, fetcher.code(), Instant.now());
        try {
            List<RawItem> items = fetcher.fetchList();
            log.info("{}: {} items on the list", fetcher.code(), items.size());
            for (RawItem item : items) {
                processItem(fetcher, item, runLog);
            }
        } catch (RuntimeException e) {
            recordError(runLog, "list", e);
            log.error("{}: source failed", fetcher.code(), e);
        }
        runLog.setDurationMs((System.nanoTime() - startNanos) / 1_000_000);
        log.info("{}: {} fetched, {} skipped, {} errors in {} ms", fetcher.code(), runLog.getFetched(),
                runLog.getSkipped(), runLog.getErrors(), runLog.getDurationMs());
        return runLog;
    }

    private void processItem(SourceFetcher fetcher, RawItem item, SourceRunLog runLog) {
        if (fetcher.needsTitleFilter() && !fundingFilter.isFunding(item.title())) {
            runLog.setSkipped(runLog.getSkipped() + 1);
            return;
        }
        try {
            RawItem detailed = fetcher.fetchDetail(item);
            upserter.upsert(fetcher.code(), detailed, ruleExtractor.extract(detailed));
            runLog.setFetched(runLog.getFetched() + 1);
        } catch (RuntimeException e) {
            recordError(runLog, item.externalId(), e);
            log.warn("{}: item {} failed: {}", fetcher.code(), item.externalId(), e.getMessage());
        }
    }

    private static void recordError(SourceRunLog runLog, String where, RuntimeException e) {
        runLog.setErrors(runLog.getErrors() + 1);
        String message = where + ": " + e.getClass().getSimpleName() + ": " + e.getMessage();
        runLog.setErrorMessage(message.length() > MAX_ERROR_MESSAGE_CHARS
                ? message.substring(0, MAX_ERROR_MESSAGE_CHARS)
                : message);
    }
}
