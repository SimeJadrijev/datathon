package com.datathon.api.seed;

import com.datathon.api.common.AdminKeyGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Save the responses into {@code src/main/resources/seed/}; see README, section on refreshing the seed. */
@RestController
@RequestMapping("/api/admin/seed")
@RequiredArgsConstructor
public class AdminSeedController {

    private final AdminKeyGuard adminKeyGuard;
    private final SeedExportService seedExportService;

    @GetMapping("/funding-calls")
    public FundingCallSnapshot exportFundingCalls(
            @RequestHeader(value = "X-Admin-Key", required = false) String adminKey) {
        adminKeyGuard.verify(adminKey);
        return seedExportService.exportFundingCalls();
    }

    @GetMapping("/regulations")
    public RegulationSnapshot exportRegulations(
            @RequestHeader(value = "X-Admin-Key", required = false) String adminKey) {
        adminKeyGuard.verify(adminKey);
        return seedExportService.exportRegulations();
    }
}
