package com.datathon.api.ingest;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("ingest")
public record IngestProperties(String cacheDir, int closedPages, boolean onStartup) {
}
