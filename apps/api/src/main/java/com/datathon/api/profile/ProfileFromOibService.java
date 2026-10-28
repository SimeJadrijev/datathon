package com.datathon.api.profile;

import com.datathon.api.fundingcall.TargetGroup;
import com.datathon.api.integration.sudreg.CompanyInfo;
import com.datathon.api.integration.sudreg.SudregClient;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfileFromOibService {

    private final SudregClient sudregClient;

    public OibProfileResponse fromOib(String oib) {
        CompanyInfo company = sudregClient.findByOib(oib);
        String county = company.seat().county();
        var profile = new ProfileRequest(
                targetGroupFor(company.legalForm()),
                List.of(),
                NkdAreas.forActivityCode(company.activityCode()),
                county,
                List.of(),
                company.activityCode() == null ? List.of() : List.of(company.activityCode()));
        return new OibProfileResponse(
                company.name(),
                new OibProfileResponse.Seat(company.seat().municipality(), county),
                company.activityCode(),
                profile,
                OibProfileResponse.SUDREG_SOURCE);
    }

    private static TargetGroup targetGroupFor(String legalForm) {
        String abbreviation = legalForm == null ? "" : legalForm.toLowerCase(Locale.ROOT);
        return switch (abbreviation) {
            case "udruga" -> TargetGroup.ASSOCIATION;
            case "ustanova" -> TargetGroup.LOCAL_GOVERNMENT;
            default -> TargetGroup.COMPANY;
        };
    }
}
