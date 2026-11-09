package com.datathon.api.eligibility;

import java.util.List;

public record EligibilityQuestion(String id, String text, EligibilityQuestionType type, List<String> options) {
}
