package com.datathon.api.seed;

import com.datathon.api.fundingcall.FundingCall;
import com.datathon.api.fundingcall.FundingCallRepository;
import com.datathon.api.regulation.Regulation;
import com.datathon.api.regulation.RegulationRepository;
import com.datathon.api.source.SourceCode;
import java.util.EnumMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Writes the seed snapshot into the database. Upserts by natural key, so running it twice is safe. */
@Slf4j
@Service
@RequiredArgsConstructor
public class SeedService {

    private final FundingCallRepository fundingCallRepository;
    private final RegulationRepository regulationRepository;

    @Transactional
    public void importFundingCalls(FundingCallSnapshot snapshot) {
        Map<SourceCode, Integer> countBySource = new EnumMap<>(SourceCode.class);
        for (SeedFundingCall seed : snapshot.fundingCalls()) {
            fundingCallRepository.save(toEntity(seed));
            countBySource.merge(seed.source(), 1, Integer::sum);
        }
        log.info("Seed {}: imported {} funding calls {}", snapshot.snapshotDate(), snapshot.fundingCalls().size(), countBySource);
    }

    @Transactional
    public void importRegulations(RegulationSnapshot snapshot) {
        for (SeedRegulation seed : snapshot.regulations()) {
            regulationRepository.save(toEntity(seed));
        }
        log.info("Seed {}: imported {} regulations", snapshot.snapshotDate(), snapshot.regulations().size());
    }

    private FundingCall toEntity(SeedFundingCall seed) {
        return fundingCallRepository.findBySourceAndExternalId(seed.source(), seed.externalId())
                .map(existing -> {
                    seed.applyTo(existing);
                    return existing;
                })
                .orElseGet(seed::toNewEntity);
    }

    private Regulation toEntity(SeedRegulation seed) {
        return regulationRepository.findByExternalId(seed.externalId())
                .map(existing -> {
                    seed.applyTo(existing);
                    return existing;
                })
                .orElseGet(seed::toNewEntity);
    }
}
