package com.datathon.api.ingest.source;

import com.datathon.api.regulation.Regulation;
import com.datathon.api.regulation.RegulationRepository;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class RegulationUpserter {

    private final RegulationRepository regulationRepository;

    @Transactional
    public void upsertAll(List<ParsedRegulation> parsed) {
        for (ParsedRegulation item : parsed) {
            Regulation regulation = regulationRepository.findByExternalId(item.externalId())
                    .orElseGet(() -> new Regulation(item.externalId(), item.title()));
            apply(regulation, item);
            regulationRepository.save(regulation);
        }
    }

    private static void apply(Regulation regulation, ParsedRegulation item) {
        boolean keepEliValues = regulation.isEliMetadata() && !item.eliMetadata();
        regulation.setTitle(item.title());
        regulation.setGazetteNumber(item.gazetteNumber());
        regulation.setDocumentNumber(item.documentNumber());
        if (!keepEliValues) {
            regulation.setType(item.type());
            regulation.setPublishedAt(item.publishedAt());
        }
        regulation.setUrl(item.url());
        regulation.setEliUrl(item.eliUrl());
        regulation.setPdfUrl(item.pdfUrl());
        if (regulation.getAreas().isEmpty()) {
            regulation.getAreas().addAll(item.areas());
        }
        if (item.eliMetadata()) {
            regulation.setEnactedAt(item.enactedAt());
            regulation.setEliMetadata(true);
        }
        regulation.setFetchedAt(Instant.now());
    }
}
