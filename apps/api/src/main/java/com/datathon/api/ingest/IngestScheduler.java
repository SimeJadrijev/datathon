package com.datathon.api.ingest;

import com.datathon.api.ai.BatchRunner;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "ingest.schedule.enabled", havingValue = "true")
@RequiredArgsConstructor
public class IngestScheduler {

    private final IngestService ingestService;
    private final BatchRunner batchRunner;

    @Scheduled(cron = "${ingest.schedule.cron}", zone = "Europe/Zagreb")
    public void runDailyIngest() {
        IngestRun run = ingestService.createRun(Optional.empty());
        try {
            batchRunner.start(IngestAdminController.BATCH_NAME, () -> ingestService.execute(run.getId()));
            log.info("Scheduled ingest run {} started", run.getId());
        } catch (IllegalStateException e) {
            ingestService.discardRun(run.getId());
            log.info("Scheduled ingest skipped: an ingest is already running");
        }
    }
}
