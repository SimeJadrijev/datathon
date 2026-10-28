package com.datathon.api.source;

import java.time.Instant;

public record SourceSummary(
        SourceCode code,
        String name,
        String description,
        String url,
        SourceMethod method,
        String license,
        Long recordCount,
        Instant lastSuccessfulFetch) {

    static SourceSummary of(SourceEntry entry, Long recordCount, Instant lastSuccessfulFetch) {
        return new SourceSummary(entry.code(), entry.name(), entry.description(), entry.url(), entry.method(),
                entry.license(), recordCount, lastSuccessfulFetch);
    }
}
