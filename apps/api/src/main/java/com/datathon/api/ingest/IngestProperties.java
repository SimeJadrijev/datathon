package com.datathon.api.ingest;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("ingest")
public record IngestProperties(String cacheDir, int closedPages, boolean onStartup,
        @DefaultValue("15") int mzomPages) {
}
