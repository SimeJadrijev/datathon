package com.datathon.api.eligibility;

import java.util.List;

/** One question about a condition of a call. {@code options} is filled only for {@link EligibilityQuestionType#CHOICE}. */
public record EligibilityQuestion(String id, String text, EligibilityQuestionType type, List<String> options) {
}
