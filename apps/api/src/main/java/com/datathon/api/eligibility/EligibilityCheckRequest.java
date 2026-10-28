package com.datathon.api.eligibility;

import com.datathon.api.fundingcall.TargetGroup;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Map;

/**
 * {@code answers} maps a question id to a Boolean, a number or a short text. {@code targetGroup} and
 * {@code county} are the optional profile, used by the rule-based fallback.
 */
public record EligibilityCheckRequest(
        @NotNull @Size(max = 10) Map<String, Object> answers,
        TargetGroup targetGroup,
        @Size(max = 100) String county) {
}
