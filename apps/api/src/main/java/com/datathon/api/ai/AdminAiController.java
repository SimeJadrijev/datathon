package com.datathon.api.ai;

import com.datathon.api.common.AdminKeyGuard;
import com.datathon.api.common.InvalidRequestException;
import com.datathon.api.eligibility.EligibilityQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/admin/ai")
@RequiredArgsConstructor
public class AdminAiController {

    private final AdminKeyGuard adminKeyGuard;
    private final FundingCallExtractionService extractionService;
    private final RegulationSummaryService summaryService;
    private final EligibilityQuestionService questionService;

    @PostMapping("/extract")
    public ResponseEntity<Void> extract(
            @RequestHeader(value = "X-Admin-Key", required = false) String adminKey,
            @RequestParam(defaultValue = "300") int limit) {
        adminKeyGuard.verify(adminKey);
        return startBatch(limit, () -> extractionService.start(limit));
    }

    @PostMapping("/summarize-regulations")
    public ResponseEntity<Void> summarizeRegulations(
            @RequestHeader(value = "X-Admin-Key", required = false) String adminKey,
            @RequestParam(defaultValue = "60") int limit) {
        adminKeyGuard.verify(adminKey);
        return startBatch(limit, () -> summaryService.start(limit));
    }

    @PostMapping("/generate-questions")
    public ResponseEntity<Void> generateQuestions(
            @RequestHeader(value = "X-Admin-Key", required = false) String adminKey,
            @RequestParam(defaultValue = "300") int limit) {
        adminKeyGuard.verify(adminKey);
        return startBatch(limit, () -> questionService.start(limit));
    }

    private ResponseEntity<Void> startBatch(int limit, Runnable start) {
        if (limit < 1) {
            throw new InvalidRequestException("limit must be at least 1", "limit: mora biti najmanje 1");
        }
        try {
            start.run();
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
        return ResponseEntity.accepted().build();
    }
}
