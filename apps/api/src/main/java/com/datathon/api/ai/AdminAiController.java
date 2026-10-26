package com.datathon.api.ai;

import com.datathon.api.common.AdminKeyGuard;
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

    @PostMapping("/extract")
    public ResponseEntity<Void> extract(
            @RequestHeader(value = "X-Admin-Key", required = false) String adminKey,
            @RequestParam(defaultValue = "300") int limit) {
        adminKeyGuard.verify(adminKey);
        try {
            extractionService.start(limit);
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
        return ResponseEntity.accepted().build();
    }
}
