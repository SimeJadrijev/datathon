package com.datathon.api.ai;

import com.datathon.api.fundingcall.FundingCall;
import com.datathon.api.fundingcall.FundingCallStatus;
import java.time.LocalDate;
import java.util.Collection;
import java.util.function.Consumer;
import org.springframework.stereotype.Component;

/**
 * The model value replaces the rule value only when the model returned a value (not null, not empty).
 * Names of the fields the model set are saved in {@code aiFields}.
 */
@Component
public class FundingCallExtractionMerger {

    public void apply(FundingCall call, FundingCallExtraction extraction) {
        call.getAiFields().clear();
        set(call, "provider", extraction.provider(), call::setProvider);
        set(call, "type", extraction.type(), call::setType);
        set(call, "publishedAt", extraction.publishedAt(), call::setPublishedAt);
        overrideDeadline(call, extraction);
        set(call, "totalAmount", extraction.totalAmount(), call::setTotalAmount);
        set(call, "minAmount", extraction.minAmount(), call::setMinAmount);
        set(call, "maxAmount", extraction.maxAmount(), call::setMaxAmount);
        set(call, "conditionsSummary", extraction.conditionsSummary(), call::setConditionsSummary);
        replace(call, "targetGroups", extraction.targetGroups(), call.getTargetGroups());
        replace(call, "areas", extraction.areas(), call.getAreas());
        replace(call, "activityCodes", extraction.activityCodes(), call.getActivityCodes());
        replace(call, "counties", extraction.counties(), call.getCounties());
        replace(call, "requiredDocuments", extraction.requiredDocuments(), call.getRequiredDocuments());
        refreshStatus(call, extraction);
        call.setExtractedByLlm(true);
    }

    private <T> void set(FundingCall call, String field, T value, Consumer<T> setter) {
        if (value == null) {
            return;
        }
        setter.accept(value);
        call.getAiFields().add(field);
    }

    private <T> void replace(FundingCall call, String field, Collection<T> values, Collection<T> target) {
        if (values.isEmpty()) {
            return;
        }
        target.clear();
        target.addAll(values);
        call.getAiFields().add(field);
    }

    /**
     * Unlike other fields, a null deadline from the model clears the rule value. The model read the whole text:
     * rules take "31. 3. 2028." for the deadline, but it is the end of the procurement period (FZOEU electric cars).
     */
    private void overrideDeadline(FundingCall call, FundingCallExtraction extraction) {
        boolean changes = extraction.deadline() != null || call.getDeadline() != null;
        call.setDeadline(extraction.deadline());
        if (changes) {
            call.getAiFields().add("deadline");
        }
    }

    /** A deadline read by the model can contradict the status the rules derived from a wrong date. */
    private void refreshStatus(FundingCall call, FundingCallExtraction extraction) {
        if (extraction.deadline() == null || call.getStatus() == FundingCallStatus.ANNOUNCED) {
            return;
        }
        boolean isOpen = !extraction.deadline().isBefore(LocalDate.now());
        call.setStatus(isOpen ? FundingCallStatus.OPEN : FundingCallStatus.CLOSED);
    }
}
