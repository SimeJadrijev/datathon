package com.datathon.api.fundingcall.dto;

import com.datathon.api.fundingcall.Area;
import com.datathon.api.fundingcall.FundingCall;
import com.datathon.api.fundingcall.FundingCallStatus;
import com.datathon.api.fundingcall.FundingCallType;
import com.datathon.api.fundingcall.TargetGroup;
import com.datathon.api.source.SourceInfo;
import com.datathon.api.source.SourceRef;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public record FundingCallSummary(
        Long id,
        String title,
        String provider,
        SourceRef source,
        FundingCallType type,
        FundingCallStatus status,
        LocalDate deadline,
        String deadlineNote,
        BigDecimal totalAmount,
        BigDecimal maxAmount,
        List<TargetGroup> targetGroups,
        List<Area> areas,
        List<String> counties,
        boolean extractedByLlm,
        List<String> aiFields,
        String url,
        Instant firstSeenAt) {

    public static FundingCallSummary from(FundingCall fundingCall, SourceInfo source) {
        return new FundingCallSummary(
                fundingCall.getId(),
                fundingCall.getTitle(),
                fundingCall.getProvider(),
                SourceRef.from(source),
                fundingCall.getType(),
                fundingCall.getStatus(),
                fundingCall.getDeadline(),
                fundingCall.getDeadlineNote(),
                fundingCall.getTotalAmount(),
                fundingCall.getMaxAmount(),
                sorted(fundingCall.getTargetGroups()),
                sorted(fundingCall.getAreas()),
                sorted(fundingCall.getCounties()),
                fundingCall.isExtractedByLlm(),
                sorted(fundingCall.getAiFields()),
                fundingCall.getUrl(),
                fundingCall.getFirstSeenAt());
    }

    static <T extends Comparable<T>> List<T> sorted(Collection<T> values) {
        return values.stream().sorted().toList();
    }
}
