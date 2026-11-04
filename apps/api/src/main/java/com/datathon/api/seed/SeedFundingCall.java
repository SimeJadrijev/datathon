package com.datathon.api.seed;

import com.datathon.api.fundingcall.Area;
import com.datathon.api.fundingcall.FundingCall;
import com.datathon.api.fundingcall.FundingCallStatus;
import com.datathon.api.fundingcall.FundingCallType;
import com.datathon.api.fundingcall.TargetGroup;
import com.datathon.api.source.SourceCode;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public record SeedFundingCall(
        SourceCode source,
        String externalId,
        String url,
        String title,
        String provider,
        FundingCallType type,
        FundingCallStatus status,
        LocalDate publishedAt,
        LocalDate deadline,
        BigDecimal totalAmount,
        BigDecimal minAmount,
        BigDecimal maxAmount,
        Set<TargetGroup> targetGroups,
        Set<Area> areas,
        Set<String> counties,
        Set<String> activityCodes,
        String conditionsSummary,
        List<String> requiredDocuments,
        List<SeedAttachment> attachments,
        String rawTextHash,
        boolean extractedByLlm,
        Set<String> aiFields,
        Instant fetchedAt,
        String sourceText,
        String deadlineNote,
        String eligibilityQuestions,
        Boolean fieldsReviewed,
        Boolean hidden,
        String hiddenReason,
        Boolean deadlineFromSource) {

    /** The export side of the seed: what the database holds now, including the AI results, so a fresh clone has them. */
    static SeedFundingCall from(FundingCall call) {
        return new SeedFundingCall(call.getSource(), call.getExternalId(), call.getUrl(), call.getTitle(),
                call.getProvider(), call.getType(), call.getStatus(), call.getPublishedAt(), call.getDeadline(),
                call.getTotalAmount(), call.getMinAmount(), call.getMaxAmount(),
                new TreeSet<>(call.getTargetGroups()), new TreeSet<>(call.getAreas()),
                new TreeSet<>(call.getCounties()), new TreeSet<>(call.getActivityCodes()),
                call.getConditionsSummary(), List.copyOf(call.getRequiredDocuments()),
                call.getAttachments().stream().map(SeedAttachment::from).toList(),
                call.getRawTextHash(), call.isExtractedByLlm(), new TreeSet<>(call.getAiFields()),
                call.getFetchedAt(), call.getSourceText(), call.getDeadlineNote(), call.getEligibilityQuestions(),
                call.isFieldsReviewed(), call.isHidden(), call.getHiddenReason(),
                call.isDeadlineFromSource());
    }

    FundingCall toNewEntity() {
        FundingCall fundingCall = new FundingCall(source, externalId, title);
        applyTo(fundingCall);
        return fundingCall;
    }

    void applyTo(FundingCall fundingCall) {
        fundingCall.setUrl(url);
        fundingCall.setTitle(title);
        fundingCall.setProvider(provider);
        fundingCall.setType(type);
        fundingCall.setStatus(status);
        fundingCall.setPublishedAt(publishedAt);
        fundingCall.setDeadline(deadline);
        fundingCall.setTotalAmount(totalAmount);
        fundingCall.setMinAmount(minAmount);
        fundingCall.setMaxAmount(maxAmount);
        replace(fundingCall.getTargetGroups(), targetGroups);
        replace(fundingCall.getAreas(), areas);
        replace(fundingCall.getCounties(), counties);
        replace(fundingCall.getActivityCodes(), activityCodes);
        fundingCall.setConditionsSummary(conditionsSummary);
        replace(fundingCall.getRequiredDocuments(), requiredDocuments);
        replace(fundingCall.getAttachments(), attachments == null ? null
                : attachments.stream().map(SeedAttachment::toAttachment).toList());
        fundingCall.setRawTextHash(rawTextHash);
        fundingCall.setExtractedByLlm(extractedByLlm);
        replace(fundingCall.getAiFields(), aiFields);
        fundingCall.setFetchedAt(fetchedAt);
        fundingCall.setSourceText(sourceText);
        fundingCall.setDeadlineNote(deadlineNote);
        fundingCall.setEligibilityQuestions(eligibilityQuestions);
        // Boxed because older seed files do not have these fields, and Jackson 3 does not map a missing value to false.
        fundingCall.setFieldsReviewed(Boolean.TRUE.equals(fieldsReviewed));
        fundingCall.setHidden(Boolean.TRUE.equals(hidden));
        fundingCall.setHiddenReason(hiddenReason);
        fundingCall.setDeadlineFromSource(Boolean.TRUE.equals(deadlineFromSource));
    }

    static <T> void replace(Collection<T> target, Collection<T> values) {
        target.clear();
        if (values != null) {
            target.addAll(values);
        }
    }
}
