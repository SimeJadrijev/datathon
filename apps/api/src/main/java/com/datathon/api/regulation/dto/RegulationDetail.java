package com.datathon.api.regulation.dto;

import com.datathon.api.fundingcall.Area;
import com.datathon.api.legalbasis.CitingFundingCall;
import com.datathon.api.regulation.Regulation;
import com.datathon.api.regulation.RegulationType;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record RegulationDetail(
        Long id,
        String title,
        RegulationType type,
        String gazetteNumber,
        Integer documentNumber,
        LocalDate publishedAt,
        LocalDate enactedAt,
        List<Area> areas,
        String summary,
        String url,
        String eliUrl,
        String pdfUrl,
        boolean eliMetadata,
        Instant fetchedAt,
        List<CitingFundingCall> citingFundingCalls) {

    public static RegulationDetail from(Regulation regulation, List<CitingFundingCall> citingFundingCalls) {
        return new RegulationDetail(
                regulation.getId(),
                regulation.getTitle(),
                regulation.getType(),
                regulation.getGazetteNumber(),
                regulation.getDocumentNumber(),
                regulation.getPublishedAt(),
                regulation.getEnactedAt(),
                regulation.getAreas().stream().sorted().toList(),
                regulation.getSummary(),
                regulation.getUrl(),
                regulation.getEliUrl(),
                regulation.getPdfUrl(),
                regulation.isEliMetadata(),
                regulation.getFetchedAt(),
                citingFundingCalls);
    }
}
