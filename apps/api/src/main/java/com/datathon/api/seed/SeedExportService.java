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

/**
 * Writes the database back into the seed format. AI results are paid for once with a key; exporting them into
 * {@code resources/seed/} lets a fresh clone (and a juror without a key) see summaries and eligibility questions.
 */
@Service
@RequiredArgsConstructor
public class SeedExportService {

    private static final ZoneId ZAGREB = ZoneId.of("Europe/Zagreb");

    private final FundingCallRepository fundingCallRepository;
    private final RegulationRepository regulationRepository;

    /**
     * Sorted by id: the seed is imported in file order, so the export keeps the order of the committed file and
     * the git diff shows only real changes. Records added later by ingest go at the end.
     */
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

    /** The newest fetch time, because the UI shows the snapshot date as "data as of". */
    private LocalDate snapshotDate(List<Instant> fetchedAt) {
        return fetchedAt.stream()
                .filter(Objects::nonNull)
                .max(Instant::compareTo)
                .map(instant -> LocalDate.ofInstant(instant, ZAGREB))
                .orElse(LocalDate.now(ZAGREB));
    }
}
