package com.datathon.api.seed;

import com.datathon.api.association.AssociationRecord;
import com.datathon.api.association.AssociationRepository;
import com.datathon.api.association.AssociationStore;
import java.util.List;
import java.util.zip.GZIPInputStream;

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
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
@ConditionalOnProperty(name = "seed.enabled", havingValue = "true", matchIfMissing = true)
public class SeedLoader implements ApplicationRunner {

    static final String FUNDING_CALLS_FILE = "seed/funding-calls.json";
    static final String REGULATIONS_FILE = "seed/regulations.json";
    static final String ASSOCIATIONS_FILE = "seed/associations.json.gz";

    private final JsonMapper jsonMapper;
    private final SeedService seedService;
    private final FundingCallRepository fundingCallRepository;
    private final RegulationRepository regulationRepository;
    private final AssociationRepository associationRepository;
    private final AssociationStore associationStore;

    @Override
    public void run(ApplicationArguments args) {
        if (fundingCallRepository.count() == 0) {
            read(FUNDING_CALLS_FILE, FundingCallSnapshot.class).ifPresent(seedService::importFundingCalls);
        }
        if (regulationRepository.count() == 0) {
            read(REGULATIONS_FILE, RegulationSnapshot.class).ifPresent(seedService::importRegulations);
        }
        if (associationRepository.count() == 0) {
            readAssociations().ifPresent(this::importAssociations);
        }
    }

    private void importAssociations(List<AssociationRecord> associations) {
        associationStore.replaceAll(associations);
        log.info("Seed: imported {} associations", associations.size());
    }

    private Optional<List<AssociationRecord>> readAssociations() {
        ClassPathResource resource = new ClassPathResource(ASSOCIATIONS_FILE);
        if (!resource.exists()) {
            return Optional.empty();
        }
        try (InputStream input = new GZIPInputStream(resource.getInputStream())) {
            return Optional.of(List.of(jsonMapper.readValue(input, AssociationRecord[].class)));
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read seed file " + ASSOCIATIONS_FILE, e);
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
