package com.datathon.api.ingest;

import com.datathon.api.fundingcall.FundingCallStatus;
import com.datathon.api.fundingcall.FundingCallType;
import com.datathon.api.fundingcall.TargetGroup;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Builder;

@Builder(toBuilder = true)
public record RawItem(
        String externalId,
        String url,
        String title,
        String provider,
        LocalDate publishedAt,
        FundingCallStatus statusHint,
        LocalDate deadlineHint,
        FundingCallType typeHint,
        List<String> categories,
        Set<String> countiesHint,
        Set<TargetGroup> targetGroupsHint,
        BigDecimal totalAmountHint,
        BigDecimal minAmountHint,
        BigDecimal maxAmountHint,
        String text,
        String pdfText,
        List<RawAttachment> attachments,
        Map<String, String> extra) {

    public RawItem {
        categories = categories == null ? List.of() : List.copyOf(categories);
        countiesHint = countiesHint == null ? Set.of() : Set.copyOf(countiesHint);
        targetGroupsHint = targetGroupsHint == null ? Set.of() : Set.copyOf(targetGroupsHint);
        text = text == null ? "" : text;
        pdfText = pdfText == null ? "" : pdfText;
        attachments = attachments == null ? List.of() : List.copyOf(attachments);
        extra = extra == null ? Map.of() : Map.copyOf(extra);
    }
}
