package com.datathon.api.ingest;

import com.datathon.api.ai.BatchRunner;
import com.datathon.api.common.AdminKeyGuard;
import com.datathon.api.source.SourceCode;
import io.swagger.v3.oas.annotations.Hidden;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Hidden
@RestController
@RequestMapping("/api/admin/ingest")
@RequiredArgsConstructor
public class IngestAdminController {

    static final String BATCH_NAME = "ingest";

    private final AdminKeyGuard adminKeyGuard;
    private final IngestService ingestService;
    private final BatchRunner batchRunner;

    @PostMapping
    public ResponseEntity<IngestRunStarted> start(
            @RequestHeader(value = "X-Admin-Key", required = false) String adminKey,
            @RequestParam(required = false) SourceCode source) {
        adminKeyGuard.verify(adminKey);
        IngestRun run = ingestService.createRun(Optional.ofNullable(source));
        try {
            batchRunner.start(BATCH_NAME, () -> ingestService.execute(run.getId()));
        } catch (IllegalStateException e) {
            ingestService.discardRun(run.getId());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ingest is already running");
        }
        return ResponseEntity.accepted().body(new IngestRunStarted(run.getId(), source));
    }
}
