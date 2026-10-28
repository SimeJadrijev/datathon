package com.datathon.api.ingest;

import com.datathon.api.source.SourceCode;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SourceRunLogRepository extends JpaRepository<SourceRunLog, Long> {

    List<SourceRunLog> findByRunId(Long runId);

    Optional<SourceRunLog> findFirstBySourceOrderByStartedAtDesc(SourceCode source);

    @Query("select log.source as source, max(log.startedAt) as startedAt from SourceRunLog log "
            + "where log.errors = 0 group by log.source")
    List<LastSuccessfulRun> findLastSuccessfulRunPerSource();

    interface LastSuccessfulRun {

        SourceCode getSource();

        Instant getStartedAt();
    }
}
