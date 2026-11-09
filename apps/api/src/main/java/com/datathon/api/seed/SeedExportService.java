package com.datathon.api.seed;

import com.datathon.api.fundingcall.FundingCallRepository;
import com.datathon.api.regulation.RegulationRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SeedExportService {

    private static final ZoneId ZAGREB = ZoneId.of("Europe/Zagreb");

    private final FundingCallRepository fundingCallRepository;
    private final RegulationRepository regulationRepository;

    @Transactional(readOnly = true)
    public FundingCallSnapshot exportFundingCalls() {
        List<SeedFundingCall> calls = fundingCallRepository.findAll(Sort.by("id")).stream()
                .map(SeedFundingCall::from)
                .toList();
        return new FundingCallSnapshot(snapshotDate(calls.stream().map(SeedFundingCall::fetchedAt).toList()), calls);
    }

    @Transactional(readOnly = true)
    public RegulationSnapshot exportRegulations() {
        List<SeedRegulation> regulations = regulationRepository.findAll(Sort.by("id")).stream()
                .map(SeedRegulation::from)
                .toList();
        return new RegulationSnapshot(snapshotDate(regulations.stream().map(SeedRegulation::fetchedAt).toList()),
                regulations);
    }

    private LocalDate snapshotDate(List<Instant> fetchedAt) {
        return fetchedAt.stream()
                .filter(Objects::nonNull)
                .max(Instant::compareTo)
                .map(instant -> LocalDate.ofInstant(instant, ZAGREB))
                .orElse(LocalDate.now(ZAGREB));
    }
}
