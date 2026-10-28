package com.datathon.api.ai;

import com.datathon.api.fundingcall.Area;
import com.datathon.api.fundingcall.FundingCall;
import com.datathon.api.fundingcall.FundingCallType;
import com.datathon.api.fundingcall.TargetGroup;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import tools.jackson.databind.JsonNode;

/** What the model read from a funding call. Null or empty means "not in the text". */
public record FundingCallExtraction(
        String provider,
        FundingCallType type,
        LocalDate publishedAt,
        LocalDate deadline,
        BigDecimal totalAmount,
        BigDecimal minAmount,
        BigDecimal maxAmount,
        Set<TargetGroup> targetGroups,
        Set<Area> areas,
        Set<String> activityCodes,
        Set<String> counties,
        String conditionsSummary,
        List<String> requiredDocuments) {

    static final int MAX_AREAS = 3;
    static final int MAX_DOCUMENTS = 10;
    static final int MIN_SUMMARY_LENGTH = 20;

    /** Lenient: an unknown enum value or a bad date is dropped, it does not fail the whole call. */
    public static FundingCallExtraction from(JsonNode node) {
        return new FundingCallExtraction(
                text(node, "provider"),
                enumValue(FundingCallType.class, text(node, "type")),
                date(node, "publishedAt"),
                date(node, "deadline"),
                number(node, "totalAmount"),
                number(node, "minAmount"),
                number(node, "maxAmount"),
                enumValues(TargetGroup.class, node.path("targetGroups"), Integer.MAX_VALUE),
                enumValues(Area.class, node.path("areas"), MAX_AREAS),
                strings(node.path("activityCodes"), Integer.MAX_VALUE),
                counties(node.path("counties")),
                summary(node, "conditionsSummary"),
                new ArrayList<>(strings(node.path("requiredDocuments"), MAX_DOCUMENTS)));
    }

    static String text(JsonNode node, String field) {
        JsonNode value = node.path(field);
        if (value.isNull() || value.isMissingNode()) {
            return null;
        }
        String text = value.asString().trim();
        return text.isEmpty() ? null : text;
    }

    private static BigDecimal number(JsonNode node, String field) {
        JsonNode value = node.path(field);
        boolean isPositive = value.isNumber() && value.decimalValue().signum() > 0;
        return isPositive ? value.decimalValue() : null;
    }

    /** A one-word "summary" is a model glitch, not a summary. */
    private static String summary(JsonNode node, String field) {
        String text = text(node, field);
        return text != null && text.length() >= MIN_SUMMARY_LENGTH ? text : null;
    }

    private static LocalDate date(JsonNode node, String field) {
        String text = text(node, field);
        if (text == null) {
            return null;
        }
        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static <E extends Enum<E>> E enumValue(Class<E> type, String name) {
        if (name == null) {
            return null;
        }
        try {
            return Enum.valueOf(type, name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    static <E extends Enum<E>> Set<E> enumValues(Class<E> type, JsonNode array, int max) {
        Set<E> values = new LinkedHashSet<>();
        for (JsonNode element : array) {
            E value = enumValue(type, element.asString());
            if (value != null && values.size() < max) {
                values.add(value);
            }
        }
        return values;
    }

    static Set<String> strings(JsonNode array, int max) {
        Set<String> values = new LinkedHashSet<>();
        for (JsonNode element : array) {
            String value = element.asString().trim();
            if (!value.isEmpty() && values.size() < max) {
                values.add(value);
            }
        }
        return values;
    }

    /** "All of Croatia" is stored as the single marker {@link FundingCall#ALL_COUNTIES}. */
    private static Set<String> counties(JsonNode array) {
        Set<String> values = strings(array, Integer.MAX_VALUE);
        boolean wholeCountry = values.stream()
                .anyMatch(county -> county.equalsIgnoreCase("ALL") || county.equalsIgnoreCase("sve"));
        return wholeCountry ? Set.of(FundingCall.ALL_COUNTIES) : values;
    }
}
