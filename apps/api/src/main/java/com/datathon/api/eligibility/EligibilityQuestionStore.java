package com.datathon.api.eligibility;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/** Questions live in {@code FundingCall.eligibilityQuestions} as raw JSON; this class is the only one that reads it. */
@Component
@RequiredArgsConstructor
@Slf4j
public class EligibilityQuestionStore {

    private final JsonMapper jsonMapper;

    public List<EligibilityQuestion> read(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return jsonMapper.readValue(json, new TypeReference<List<EligibilityQuestion>>() { });
        } catch (JacksonException e) {
            log.warn("Stored eligibility questions are not valid JSON, treating as none: {}", e.getMessage());
            return List.of();
        }
    }

    public String write(List<EligibilityQuestion> questions) {
        return jsonMapper.writeValueAsString(questions);
    }
}
