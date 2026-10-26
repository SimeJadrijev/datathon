package com.datathon.api.seed;

import com.datathon.api.fundingcall.Area;
import com.datathon.api.regulation.Regulation;
import com.datathon.api.regulation.RegulationType;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;

/** One regulation in the seed snapshot. Field names match {@link Regulation}. */
public record SeedRegulation(
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
        boolean eliMetadata,
        Instant fetchedAt) {

    Regulation toNewEntity() {
        Regulation regulation = new Regulation(externalId, title);
        applyTo(regulation);
        return regulation;
    }

    /** Copies the snapshot values onto an entity, keeping the AI summary if there is one. */
    void applyTo(Regulation regulation) {
        regulation.setTitle(title);
        regulation.setType(type);
        regulation.setGazetteNumber(gazetteNumber);
        regulation.setDocumentNumber(documentNumber);
        regulation.setPublishedAt(publishedAt);
        regulation.setEnactedAt(enactedAt);
        regulation.setUrl(url);
        regulation.setEliUrl(eliUrl);
        regulation.setPdfUrl(pdfUrl);
        SeedFundingCall.replace(regulation.getAreas(), areas);
        regulation.setEliMetadata(eliMetadata);
        regulation.setFetchedAt(fetchedAt);
    }
}
