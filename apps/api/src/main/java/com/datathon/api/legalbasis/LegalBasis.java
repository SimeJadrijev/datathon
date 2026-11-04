package com.datathon.api.legalbasis;

import com.datathon.api.fundingcall.LegalReference;
import com.datathon.api.regulation.GazetteCitationParser;
import com.datathon.api.regulation.Regulation;

public record LegalBasis(
        String citedTitle,
        String gazetteNumber,
        String gazetteUrl,
        Long regulationId,
        String regulationTitle,
        String regulationEliUrl) {

    static LegalBasis of(LegalReference reference, Regulation regulation) {
        return new LegalBasis(
                reference.getCitedTitle(),
                reference.getGazetteNumber(),
                GazetteCitationParser.issueUrl(reference.getGazetteNumber()),
                regulation == null ? null : regulation.getId(),
                regulation == null ? null : regulation.getTitle(),
                regulation == null ? null : regulation.getEliUrl());
    }
}
