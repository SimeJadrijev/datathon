package com.datathon.api.seed;

import com.datathon.api.fundingcall.FundingCallRepository;
import com.datathon.api.regulation.RegulationRepository;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * Fills empty tables from the snapshot in {@code resources/seed} on startup, so a clean checkout
 * has data without running the scrapers or depending on other people's servers during the demo.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "seed.enabled", havingValue = "true", matchIfMissing = true)
public class SeedLoader implements ApplicationRunner {

    static final String FUNDING_CALLS_FILE = "seed/funding-calls.json";
    static final String REGULATIONS_FILE = "seed/regulations.json";

    private final JsonMapper jsonMapper;
    private final SeedService seedService;
    private final FundingCallRepository fundingCallRepository;
    private final RegulationRepository regulationRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (fundingCallRepository.count() == 0) {
            read(FUNDING_CALLS_FILE, FundingCallSnapshot.class).ifPresent(seedService::importFundingCalls);
        }
        if (regulationRepository.count() == 0) {
            read(REGULATIONS_FILE, RegulationSnapshot.class).ifPresent(seedService::importRegulations);
        }
    }

    private <T> Optional<T> read(String path, Class<T> type) {
        ClassPathResource resource = new ClassPathResource(path);
        if (!resource.exists()) {
            log.warn("Seed file {} not found, skipping", path);
            return Optional.empty();
        }
        try (InputStream input = resource.getInputStream()) {
            return Optional.of(jsonMapper.readValue(input, type));
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read seed file " + path, e);
        }
    }
}
