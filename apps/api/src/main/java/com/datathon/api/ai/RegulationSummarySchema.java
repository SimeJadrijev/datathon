package com.datathon.api.ai;

import com.datathon.api.fundingcall.Area;
import com.datathon.api.integration.llm.ResponseSchema;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

final class RegulationSummarySchema {

    static final String PURPOSE = "summarize-regulation";

    static final String SYSTEM_PROMPT = """
            From the text of a Croatian law or regulation (Narodne novine) write a short summary for citizens and businesses.
            Rules:
            - Use only the text you were given. Do not add general knowledge and do not guess.
            - summary: 2-3 sentences in Croatian: what the act regulates, whom it affects, and when it applies (entry into force) if the text says it. Leave out any part the text does not answer; never write that the text does not say or contain something.
            - If the text does not allow a summary, return summary = null.
            - areas: the 3 most important areas the act clearly concerns. Empty list if it does not clearly belong to any.""";

    static final ResponseSchema SCHEMA = ResponseSchema.builder()
            .property("summary", Map.of("type", List.of("string", "null")))
            .property("areas", Map.of("type", "array",
                    "items", Map.of("type", "string", "enum",
                            Arrays.stream(Area.values()).map(Enum::name).toList())))
            .requiredField("summary")
            .requiredField("areas")
            .build();

    private RegulationSummarySchema() {
    }
}
