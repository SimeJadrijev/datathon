package com.datathon.api.ingest;

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
    private final FundingFilter fundingFilter;
    private final RuleExtractor ruleExtractor;
    private final FundingCallUpserter upserter;
    private final IngestRunRepository ingestRunRepository;
    private final SourceRunLogRepository sourceRunLogRepository;

    public IngestRun createRun(Optional<SourceCode> source) {
        return ingestRunRepository.save(new IngestRun(Instant.now(), source.orElse(null)));
    }

    public void execute(Long runId) {
        IngestRun run = ingestRunRepository.findById(runId)
                .orElseThrow(() -> new IllegalArgumentException("Ingest run " + runId + " does not exist"));
        for (SourceFetcher fetcher : selectedFetchers(run.getRequestedSource())) {
            sourceRunLogRepository.save(runSource(run, fetcher));
        }
        run.setFinishedAt(Instant.now());
        ingestRunRepository.save(run);
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
        if (!fundingFilter.isFunding(item.title())) {
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
