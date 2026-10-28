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
        Set<TargetGroup> targetGroups,
        Set<String> counties,
        List<EligibilityQuestion> questions) {
}
