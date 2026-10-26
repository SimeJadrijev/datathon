package com.datathon.api.eligibility;

/** {@code quote} is a literal passage from the call text; null only for reasons from the rule-based fallback. */
public record EligibilityReason(String text, String quote) {
}
