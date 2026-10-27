package com.datathon.api.fundingcall.dto;

import static com.datathon.api.fundingcall.dto.FundingCallSummary.sorted;

import com.datathon.api.fundingcall.Area;
import com.datathon.api.fundingcall.FundingCall;
import com.datathon.api.fundingcall.FundingCallStatus;
import com.datathon.api.fundingcall.FundingCallType;
import com.datathon.api.fundingcall.TargetGroup;
import com.datathon.api.source.SourceInfo;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record FundingCallDetail(
        Long id,
        String title,
        String provider,
        SourceInfo source,
        FundingCallType type,
        FundingCallStatus status,
        LocalDate publishedAt,
        LocalDate deadline,
        String deadlineNote,
        BigDecimal totalAmount,
        BigDecimal minAmount,
        BigDecimal maxAmount,
        List<TargetGroup> targetGroups,
        List<Area> areas,
        List<String> counties,
        String conditionsSummary,
        List<String> requiredDocuments,
        List<AttachmentResponse> attachments,
        boolean extractedByLlm,
        List<String> aiFields,
        boolean fieldsReviewed,
        Instant fetchedAt,
        String url) {

    public static FundingCallDetail from(FundingCall fundingCall, SourceInfo source) {
        return new FundingCallDetail(
                fundingCall.getId(),
                fundingCall.getTitle(),
                fundingCall.getProvider(),
                source,
                fundingCall.getType(),
                fundingCall.getStatus(),
                fundingCall.getPublishedAt(),
                fundingCall.getDeadline(),
                fundingCall.getDeadlineNote(),
                fundingCall.getTotalAmount(),
                fundingCall.getMinAmount(),
                fundingCall.getMaxAmount(),
                sorted(fundingCall.getTargetGroups()),
                sorted(fundingCall.getAreas()),
                sorted(fundingCall.getCounties()),
                fundingCall.getConditionsSummary(),
                List.copyOf(fundingCall.getRequiredDocuments()),
                fundingCall.getAttachments().stream().map(AttachmentResponse::from).toList(),
                fundingCall.isExtractedByLlm(),
                sorted(fundingCall.getAiFields()),
                fundingCall.isFieldsReviewed(),
                fundingCall.getFetchedAt(),
                fundingCall.getUrl());
    }
}
