package com.datathon.api.eligibility;

import com.datathon.api.integration.llm.ResponseSchema;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/** Schema and prompt for judging an applicant's answers against the text of a call. */
final class EligibilityJudgeSchema {

    static final String PURPOSE = "eligibility-check";

    static final String SYSTEM_PROMPT = """
            You judge whether an applicant likely meets the conditions of a Croatian funding call, using only the text of the call and the applicant's answers.
            Rules:
            - Use only the call text. Do not add general knowledge and do not guess.
            - Every reason must carry a quote: a passage copied word for word from the call text (at most 300 characters, no ellipsis, no changes). If you cannot quote the condition, do not give the reason.
            - LIKELY_NOT_ELIGIBLE only when an answer clearly violates a condition that you can quote. LIKELY_ELIGIBLE when the applicant answered the questions asked and every answer satisfies its condition, and no answer or passage of the text contradicts it; conditions the applicant was not asked about go to missingInfo and do not by themselves prevent LIKELY_ELIGIBLE. UNCLEAR when the answers are mixed, a central condition cannot be judged, or the text does not settle it.
            - Never state that the applicant will surely qualify or surely fail. This is an indication, not a decision.
            - reasons: at most 5, each a short sentence in Croatian saying how an answer relates to the quoted condition.
            - missingInfo: short sentences in Croatian about what the applicant did not say or what the text does not settle. Empty list if nothing is missing.""";

    static final ResponseSchema SCHEMA = ResponseSchema.builder()
            .property("verdict", Map.of("type", "string", "enum",
                    Arrays.stream(EligibilityVerdict.values()).map(Enum::name).toList()))
            .property("reasons", Map.of("type", "array", "items", Map.of(
                    "type", "object",
                    "properties", Map.of("text", Map.of("type", "string"), "quote", Map.of("type", "string")),
                    "required", List.of("text", "quote"),
                    "additionalProperties", false)))
            .property("missingInfo", Map.of("type", "array", "items", Map.of("type", "string")))
            .requiredField("verdict")
            .requiredField("reasons")
            .requiredField("missingInfo")
            .build();

    private EligibilityJudgeSchema() {
    }
}
