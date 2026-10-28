package com.datathon.api.regulation.dto;

import com.datathon.api.fundingcall.Area;
import com.datathon.api.regulation.Regulation;
import com.datathon.api.regulation.RegulationType;
import java.time.LocalDate;
import java.util.List;

public record RegulationSummary(
        Long id,
        String title,
        RegulationType type,
        String gazetteNumber,
        LocalDate publishedAt,
        List<Area> areas,
        boolean hasSummary) {

    public static RegulationSummary from(Regulation regulation) {
        return new RegulationSummary(
                regulation.getId(),
                regulation.getTitle(),
                regulation.getType(),
                regulation.getGazetteNumber(),
                regulation.getPublishedAt(),
                regulation.getAreas().stream().sorted().toList(),
                regulation.getSummary() != null && !regulation.getSummary().isBlank());
    }
}
