package com.datathon.api.seed;

import com.datathon.api.association.AssociationRecord;
import com.datathon.api.association.AssociationStore;
import com.datathon.api.common.AdminKeyGuard;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/seed")
@RequiredArgsConstructor
public class AdminSeedController {

    private final AdminKeyGuard adminKeyGuard;
    private final SeedExportService seedExportService;
    private final AssociationStore associationStore;

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

    @GetMapping("/associations")
    public List<AssociationRecord> exportAssociations(
            @RequestHeader(value = "X-Admin-Key", required = false) String adminKey) {
        adminKeyGuard.verify(adminKey);
        return associationStore.exportAll();
    }
}
