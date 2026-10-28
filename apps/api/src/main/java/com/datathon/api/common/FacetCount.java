package com.datathon.api.common;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public record FacetCount(String value, long count) {

    private static final Comparator<FacetCount> MOST_FREQUENT_FIRST =
            Comparator.comparingLong(FacetCount::count).reversed().thenComparing(FacetCount::value);

    public static List<FacetCount> sortedByCount(Map<String, Long> countsByValue) {
        return countsByValue.entrySet().stream()
                .map(entry -> new FacetCount(entry.getKey(), entry.getValue()))
                .sorted(MOST_FREQUENT_FIRST)
                .toList();
    }
}
