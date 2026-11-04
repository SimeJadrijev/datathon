package com.datathon.api.fundingcall.dto;

import com.datathon.api.fundingcall.Area;
import com.datathon.api.fundingcall.FundingCallType;
import com.datathon.api.fundingcall.TargetGroup;
import com.datathon.api.source.SourceCode;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public record FundingCallFilter(
        @Size(max = 200, message = "smije imati najviše 200 znakova") String q,
        FundingCallStatusFilter status,
        TargetGroup targetGroup,
        Area area,
        String county,
        SourceCode source,
        FundingCallType type,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deadlineBefore,
        Integer page,
        Integer size,
        FundingCallSort sort) {

    public FundingCallFilter {
        status = status == null ? FundingCallStatusFilter.OPEN : status;
        sort = sort == null ? FundingCallSort.DEADLINE : sort;
        county = county == null || county.isBlank() ? null : county.strip();
        q = q == null || q.isBlank() ? null : q;
    }
}
