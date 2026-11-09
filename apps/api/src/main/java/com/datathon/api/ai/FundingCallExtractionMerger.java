package com.datathon.api.ai;

import com.datathon.api.common.CountyNames;
import com.datathon.api.fundingcall.FundingCall;
import com.datathon.api.fundingcall.FundingCallStatuses;
import com.datathon.api.ingest.extract.DeadlineFinder;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.function.Consumer;
import org.springframework.stereotype.Component;

@Component
public class FundingCallExtractionMerger {

    public void apply(FundingCall call, FundingCallExtraction extraction) {
        call.getAiFields().clear();
        if (call.isFieldsReviewed()) {
            applyTextOnly(call, extraction);
            return;
        }
        setIfMissing(call, "provider", call.getProvider(), extraction.provider(), call::setProvider);
        set(call, "type", extraction.type(), call::setType);
        setIfMissing(call, "publishedAt", call.getPublishedAt(), pastOrNull(extraction.publishedAt()), call::setPublishedAt);
        overrideDeadline(call, extraction);
        set(call, "totalAmount", extraction.totalAmount(), call::setTotalAmount);
        set(call, "minAmount", extraction.minAmount(), call::setMinAmount);
        set(call, "maxAmount", extraction.maxAmount(), call::setMaxAmount);
        set(call, "conditionsSummary", extraction.conditionsSummary(), call::setConditionsSummary);
        replace(call, "targetGroups", extraction.targetGroups(), call.getTargetGroups());
        replace(call, "areas", extraction.areas(), call.getAreas());
        replace(call, "activityCodes", extraction.activityCodes(), call.getActivityCodes());
        if (!CountyNames.areLocal(call.getCounties())) {
            replace(call, "counties", extraction.counties(), call.getCounties());
        }
        replace(call, "requiredDocuments", extraction.requiredDocuments(), call.getRequiredDocuments());
        orderAmounts(call);
        refreshStatus(call, extraction);
        call.setExtractedByLlm(true);
    }

    private void applyTextOnly(FundingCall call, FundingCallExtraction extraction) {
        set(call, "conditionsSummary", extraction.conditionsSummary(), call::setConditionsSummary);
        replace(call, "requiredDocuments", extraction.requiredDocuments(), call.getRequiredDocuments());
        call.setExtractedByLlm(true);
    }

    private <T> void set(FundingCall call, String field, T value, Consumer<T> setter) {
        if (value == null) {
            return;
        }
        setter.accept(value);
        call.getAiFields().add(field);
    }

    private <T> void setIfMissing(FundingCall call, String field, Object current, T value, Consumer<T> setter) {
        if (current == null) {
            set(call, field, value, setter);
        }
    }

    private static LocalDate pastOrNull(LocalDate publishedAt) {
        return publishedAt != null && publishedAt.isAfter(LocalDate.now()) ? null : publishedAt;
    }

    private <T> void replace(FundingCall call, String field, Collection<T> values, Collection<T> target) {
        if (values.isEmpty()) {
            return;
        }
        target.clear();
        target.addAll(values);
        call.getAiFields().add(field);
    }

    private void overrideDeadline(FundingCall call, FundingCallExtraction extraction) {
        if (call.isDeadlineFromSource()) {
            return;
        }
        if (extraction.deadline() == null && call.getDeadlineNote() == null) {
            DeadlineFinder.findNote(call.getSourceText()).ifPresent(call::setDeadlineNote);
        }
        boolean changes = extraction.deadline() != null || call.getDeadline() != null;
        call.setDeadline(extraction.deadline());
        if (changes) {
            call.getAiFields().add("deadline");
        }
    }

    private void refreshStatus(FundingCall call, FundingCallExtraction extraction) {
        if (call.isDeadlineFromSource() || extraction.deadline() == null
                || FundingCallStatuses.isCancelled(call.getTitle())) {
            return;
        }
        call.setStatus(FundingCallStatuses.fromDeadline(extraction.deadline(), call.getStatus(), LocalDate.now()));
    }

    private void orderAmounts(FundingCall call) {
        if (call.getMinAmount() != null && call.getMaxAmount() != null
                && call.getMinAmount().compareTo(call.getMaxAmount()) > 0) {
            BigDecimal min = call.getMaxAmount();
            call.setMaxAmount(call.getMinAmount());
            call.setMinAmount(min);
        }
    }
}
