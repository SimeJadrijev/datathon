package com.datathon.api.regulation.dto;

import com.datathon.api.common.FacetCount;
import java.util.List;

public record RegulationFacets(List<FacetCount> type, List<FacetCount> area) {
}
