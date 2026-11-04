package com.datathon.api.eligibility;

import com.datathon.api.fundingcall.TargetGroup;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/** Everything the check needs from a funding call, copied out so no lazy collection is touched later. */
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

    /** The model only reads the conditions and does not know today's date, so an expired call is caught here. */
    public boolean isDeadlinePassed(LocalDate today) {
        return deadline != null && deadline.isBefore(today);
    }

    public boolean isClosedForApplications(LocalDate today) {
        return closed || isDeadlinePassed(today);
    }
}
