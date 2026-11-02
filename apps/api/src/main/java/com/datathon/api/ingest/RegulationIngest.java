package com.datathon.api.ingest;

import com.datathon.api.source.SourceCode;

public interface RegulationIngest {

    record Outcome(int fetched, int errors, String errorMessage) {
    }

    SourceCode code();

    Outcome ingest();
}
