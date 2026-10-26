package com.datathon.api.ingest;

import com.datathon.api.source.SourceCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "source_run_log")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SourceRunLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ingest_run_id", nullable = false)
    private IngestRun run;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private SourceCode source;

    @Column(nullable = false)
    private Instant startedAt;

    private int fetched;

    private int skipped;

    private int errors;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    private long durationMs;

    public SourceRunLog(IngestRun run, SourceCode source, Instant startedAt) {
        this.run = run;
        this.source = source;
        this.startedAt = startedAt;
    }
}
