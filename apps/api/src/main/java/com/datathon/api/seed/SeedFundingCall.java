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

/** One funding call in the seed snapshot. Field names match {@link FundingCall}. */
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
        String sourceText) {

    FundingCall toNewEntity() {
        FundingCall fundingCall = new FundingCall(source, externalId, title);
        applyTo(fundingCall);
        return fundingCall;
    }

    /** Copies the snapshot values onto an entity, keeping fields the snapshot does not know (review, hidden). */
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
    }

    // Clear and refill instead of setting a new collection, so Hibernate keeps tracking the managed one
    static <T> void replace(Collection<T> target, Collection<T> values) {
        target.clear();
        if (values != null) {
            target.addAll(values);
        }
    }
}
