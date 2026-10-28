package com.datathon.api.fundingcall.dto;

import java.util.Arrays;
import java.util.Optional;
import org.springframework.data.domain.Sort;

public enum FundingCallSort {
    DEADLINE("deadline", Sort.by(Sort.Order.asc("deadline").nullsLast(), Sort.Order.asc("id"))),
    PUBLISHED_AT("publishedAt", Sort.by(Sort.Order.desc("publishedAt").nullsLast(), Sort.Order.desc("id")));

    private final String parameterValue;
    private final Sort sort;

    FundingCallSort(String parameterValue, Sort sort) {
        this.parameterValue = parameterValue;
        this.sort = sort;
    }

    public Sort toSort() {
        return sort;
    }

    public static Optional<FundingCallSort> fromParameterValue(String value) {
        return Arrays.stream(values()).filter(candidate -> candidate.parameterValue.equals(value)).findFirst();
    }
}
