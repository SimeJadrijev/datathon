package com.datathon.api.ingest;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngestRunRepository extends JpaRepository<IngestRun, Long> {

    Optional<IngestRun> findFirstByFinishedAtIsNotNullOrderByFinishedAtDesc();
}
