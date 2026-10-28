package com.datathon.api.eligibility;

import com.datathon.api.integration.llm.ResponseSchema;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/** Schema and prompt for writing the questions an applicant answers to see if they fit a call. */
final class EligibilityQuestionSchema {

    static final String PURPOSE = "eligibility-questions";
    static final int MAX_QUESTIONS = 5;

    static final String SYSTEM_PROMPT = """
            From the text of a Croatian funding call write up to 5 short questions an applicant answers to check whether they meet the conditions of the call.
            Rules:
            - Ask only about conditions that are explicitly written in the text (who may apply, residence or seat, age, registered activity, minimum or maximum values, required status). Never invent a condition.
            - Write each question in Croatian, one short sentence, so that a citizen or a business owner understands it.
            - Phrase every YES_NO question so that YES means the condition is met. Avoid negations ("Nemate li...") and "or" questions.
            - type YES_NO for a yes or no answer, NUMBER when the answer is a number (age, number of employees, amount), CHOICE when the answer is one of a few named options; fill options only for CHOICE (2 to 6 short options), otherwise return an empty list.
            - The most important conditions first. If the text names no checkable conditions, return an empty list.""";

    static final ResponseSchema SCHEMA = ResponseSchema.builder()
            .property("questions", Map.of("type", "array", "items", questionItem()))
            .requiredField("questions")
            .build();

    private EligibilityQuestionSchema() {
    }

    private static Map<String, Object> questionItem() {
        List<String> types = Arrays.stream(EligibilityQuestionType.values()).map(Enum::name).toList();
        return Map.of(
                "type", "object",
                "properties", Map.of(
                        "text", Map.of("type", "string"),
                        "type", Map.of("type", "string", "enum", types),
                        "options", Map.of("type", "array", "items", Map.of("type", "string"))),
                "required", List.of("text", "type", "options"),
                "additionalProperties", false);
    }
}
