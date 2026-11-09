package com.datathon.api.eligibility;

import com.datathon.api.fundingcall.TargetGroup;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public record EligibilityContext(
        Long id,
        String title,
        String url,
        String sourceText,
        LocalDate deadline,
        boolean closed,
        Set<TargetGroup> targetGroups,
        Set<String> counties,
        List<EligibilityQuestion> questions) {

    public boolean isDeadlinePassed(LocalDate today) {
        return deadline != null && deadline.isBefore(today);
    }

    public boolean isClosedForApplications(LocalDate today) {
        return closed || isDeadlinePassed(today);
    }
}
