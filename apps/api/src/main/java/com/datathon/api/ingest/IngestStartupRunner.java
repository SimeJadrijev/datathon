package com.datathon.api.ingest;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ingest.on-startup", havingValue = "true")
public class IngestStartupRunner implements ApplicationRunner {

    private final IngestService ingestService;

    @Override
    public void run(ApplicationArguments args) {
        ingestService.runNow(Optional.empty());
    }
}
