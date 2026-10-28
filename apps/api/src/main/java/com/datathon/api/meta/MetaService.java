package com.datathon.api.meta;

import static com.datathon.api.fundingcall.FundingCallSpecifications.hasStatus;
import static com.datathon.api.fundingcall.FundingCallSpecifications.visible;

import com.datathon.api.fundingcall.FundingCallRepository;
import com.datathon.api.fundingcall.dto.FundingCallStatusFilter;
import com.datathon.api.ingest.IngestRun;
import com.datathon.api.ingest.IngestRunRepository;
import com.datathon.api.regulation.RegulationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MetaService {

    private final SnapshotDate snapshotDate;
    private final IngestRunRepository ingestRunRepository;
    private final FundingCallRepository fundingCallRepository;
    private final RegulationRepository regulationRepository;

    @Transactional(readOnly = true)
    public MetaResponse get() {
        var counts = new MetaResponse.Counts(
                fundingCallRepository.count(visible().and(hasStatus(FundingCallStatusFilter.OPEN))),
                regulationRepository.count());
        return new MetaResponse(
                snapshotDate.value().orElse(null),
                ingestRunRepository.findFirstByFinishedAtIsNotNullOrderByFinishedAtDesc()
                        .map(IngestRun::getFinishedAt)
                        .orElse(null),
                counts);
    }
}
