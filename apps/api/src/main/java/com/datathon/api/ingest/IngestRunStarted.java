package com.datathon.api.ingest;

import com.datathon.api.source.SourceCode;

public record IngestRunStarted(Long runId, SourceCode source) {
}
