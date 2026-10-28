package com.datathon.api.fundingcall.dto;

import com.datathon.api.common.FacetCount;
import java.util.List;

public record FundingCallFacets(
        List<FacetCount> status,
        List<FacetCount> targetGroup,
        List<FacetCount> area,
        List<FacetCount> county,
        List<FacetCount> source,
        List<FacetCount> type) {
}
