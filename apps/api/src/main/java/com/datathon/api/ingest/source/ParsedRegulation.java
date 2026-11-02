package com.datathon.api.ingest.source;

import com.datathon.api.fundingcall.Area;
import com.datathon.api.regulation.RegulationType;
import java.time.LocalDate;
import java.util.Set;
import lombok.Builder;

@Builder(toBuilder = true)
public record ParsedRegulation(
        String externalId,
        String title,
        RegulationType type,
        String gazetteNumber,
        Integer documentNumber,
        LocalDate publishedAt,
        LocalDate enactedAt,
        String url,
        String eliUrl,
        String pdfUrl,
        Set<Area> areas,
        boolean eliMetadata) {

    public ParsedRegulation {
        areas = areas == null ? Set.of() : Set.copyOf(areas);
    }
}
