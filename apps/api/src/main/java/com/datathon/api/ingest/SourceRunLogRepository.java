package com.datathon.api.ingest;

import com.datathon.api.source.SourceCode;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SourceRunLogRepository extends JpaRepository<SourceRunLog, Long> {

    List<SourceRunLog> findByRunId(Long runId);

    Optional<SourceRunLog> findFirstBySourceOrderByStartedAtDesc(SourceCode source);
}
