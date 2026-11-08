package com.datathon.api.eligibility;

import java.util.List;

public record EligibilityCheckResponse(
        EligibilityVerdict verdict,
        List<EligibilityReason> reasons,
        List<String> missingInfo,
        String disclaimer,
        String sourceUrl) {

    public static final String DISCLAIMER = "Informativna procjena na temelju teksta poziva, nije pravni savjet. "
            + "Provjeri uvjete u službenoj dokumentaciji.";
}
