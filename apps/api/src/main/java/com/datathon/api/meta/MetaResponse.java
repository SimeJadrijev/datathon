package com.datathon.api.meta;

import java.time.Instant;
import java.time.LocalDate;

public record MetaResponse(LocalDate snapshotDate, Instant lastIngestAt, Counts counts) {

    public record Counts(long openFundingCalls, long regulations) {
    }
}
