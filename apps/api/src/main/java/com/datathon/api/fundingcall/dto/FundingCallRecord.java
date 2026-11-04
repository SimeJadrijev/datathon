package com.datathon.api.fundingcall.dto;

import static com.datathon.api.fundingcall.dto.FundingCallSummary.sorted;

import com.datathon.api.fundingcall.Area;
import com.datathon.api.fundingcall.FundingCall;
import com.datathon.api.fundingcall.FundingCallStatus;
import com.datathon.api.fundingcall.FundingCallType;
import com.datathon.api.fundingcall.TargetGroup;
import com.datathon.api.legalbasis.LegalBasis;
import com.datathon.api.source.SourceInfo;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record FundingCallRecord(
        String id,
        String title,
        Provider provider,
        FundingCallType type,
        FundingCallStatus status,
        LocalDate publishedAt,
        LocalDate deadline,
        String deadlineNote,
        Amounts amounts,
        List<TargetGroup> targetGroups,
        List<Area> areas,
        List<String> activityCodes,
        List<String> counties,
        String conditionsSummary,
        List<LegalAct> legalBasis,
        List<Document> attachments,
        Origin origin,
        Instant updatedAt) {

    public record Provider(String name, String oib) {
    }

    public record Amounts(BigDecimal total, BigDecimal minPerApplicant, BigDecimal maxPerApplicant, String currency) {
    }

    public record LegalAct(String citedTitle, String gazetteNumber, String gazetteIssueUri, String eli) {
    }

    public record Document(String name, String url) {
    }

    public record Origin(String sourceCode, String sourceName, String sourceUrl, String url) {
    }

    public static FundingCallRecord from(FundingCall fundingCall, SourceInfo source, List<LegalBasis> legalBasis) {
        return new FundingCallRecord(
                fundingCall.getUrl() != null ? fundingCall.getUrl() : source.url() + "#" + fundingCall.getExternalId(),
                fundingCall.getTitle(),
                new Provider(fundingCall.getProvider(), null),
                fundingCall.getType(),
                fundingCall.getStatus(),
                fundingCall.getPublishedAt(),
                fundingCall.getDeadline(),
                fundingCall.getDeadlineNote(),
                new Amounts(fundingCall.getTotalAmount(), fundingCall.getMinAmount(), fundingCall.getMaxAmount(), "EUR"),
                sorted(fundingCall.getTargetGroups()),
                sorted(fundingCall.getAreas()),
                sorted(fundingCall.getActivityCodes()),
                sorted(fundingCall.getCounties()),
                fundingCall.getConditionsSummary(),
                legalBasis.stream()
                        .map(act -> new LegalAct(act.citedTitle(), act.gazetteNumber(), act.gazetteUrl(), act.regulationEliUrl()))
                        .toList(),
                fundingCall.getAttachments().stream().map(file -> new Document(file.getName(), file.getUrl())).toList(),
                new Origin(source.code().name(), source.name(), source.url(), fundingCall.getUrl()),
                fundingCall.getFetchedAt());
    }
}
