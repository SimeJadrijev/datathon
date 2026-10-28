package com.datathon.api.source;

import static com.datathon.api.fundingcall.FundingCallSpecifications.fromSource;
import static com.datathon.api.fundingcall.FundingCallSpecifications.visible;

import com.datathon.api.fundingcall.FundingCallRepository;
import com.datathon.api.ingest.SourceRunLogRepository;
import com.datathon.api.ingest.SourceRunLogRepository.LastSuccessfulRun;
import com.datathon.api.meta.SnapshotDate;
import com.datathon.api.regulation.RegulationRepository;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SourceService {

    private final SourceCatalog sourceCatalog;
    private final FundingCallRepository fundingCallRepository;
    private final RegulationRepository regulationRepository;
    private final SourceRunLogRepository sourceRunLogRepository;
    private final SnapshotDate snapshotDate;

    @Transactional(readOnly = true)
    public List<SourceSummary> list() {
        Map<SourceCode, Instant> lastSuccessfulRuns = lastSuccessfulRuns();
        Instant snapshotInstant = snapshotDate.value()
                .map(date -> date.atStartOfDay(ZoneOffset.UTC).toInstant())
                .orElse(null);
        return sourceCatalog.entries().stream()
                .map(entry -> SourceSummary.of(entry, recordCount(entry.code()),
                        lastFetch(entry.code(), lastSuccessfulRuns, snapshotInstant)))
                .toList();
    }

    private static Instant lastFetch(SourceCode code, Map<SourceCode, Instant> lastSuccessfulRuns, Instant snapshotInstant) {
        if (code == SourceCode.SUDREG) {
            return null;
        }
        return lastSuccessfulRuns.getOrDefault(code, snapshotInstant);
    }

    private Long recordCount(SourceCode code) {
        return switch (code) {
            case NARODNE_NOVINE -> regulationRepository.count();
            case SUDREG -> null;
            default -> fundingCallRepository.count(visible().and(fromSource(code)));
        };
    }

    private Map<SourceCode, Instant> lastSuccessfulRuns() {
        Map<SourceCode, Instant> byCode = new EnumMap<>(SourceCode.class);
        for (LastSuccessfulRun run : sourceRunLogRepository.findLastSuccessfulRunPerSource()) {
            byCode.put(run.getSource(), run.getStartedAt());
        }
        return byCode;
    }
}
