package com.datathon.api.ai;

import com.datathon.api.fundingcall.Area;
import java.util.Set;
import tools.jackson.databind.JsonNode;

/** What the model read from a regulation. Null or empty means "not in the text". */
public record RegulationSummary(String summary, Set<Area> areas) {

    static final int MAX_AREAS = 3;
    static final int MIN_SUMMARY_LENGTH = 20;

    public static RegulationSummary from(JsonNode node) {
        String summary = FundingCallExtraction.text(node, "summary");
        boolean isUsable = summary != null && summary.length() >= MIN_SUMMARY_LENGTH;
        return new RegulationSummary(
                isUsable ? summary : null,
                FundingCallExtraction.enumValues(Area.class, node.path("areas"), MAX_AREAS));
    }
}
