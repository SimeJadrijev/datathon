package com.datathon.api.ingest.extract;

import com.datathon.api.common.CountyNames;
import com.datathon.api.fundingcall.Area;
import com.datathon.api.fundingcall.FundingCallStatus;
import com.datathon.api.fundingcall.FundingCallType;
import com.datathon.api.fundingcall.TargetGroup;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

public record ExtractedFields(
        FundingCallType type,
        FundingCallStatus status,
        LocalDate publishedAt,
        LocalDate deadline,
        String deadlineNote,
        BigDecimal totalAmount,
        BigDecimal minAmount,
        BigDecimal maxAmount,
        Set<TargetGroup> targetGroups,
        Set<Area> areas,
        Set<String> activityCodes,
        Set<String> counties) {

    public ExtractedFields {
        counties = CountyNames.canonical(counties);
    }
}
