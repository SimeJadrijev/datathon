package com.datathon.api.ingest;

import com.datathon.api.source.SourceCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ingest_run")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IngestRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Instant startedAt;

    private Instant finishedAt;

    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private SourceCode requestedSource;

    public IngestRun(Instant startedAt, SourceCode requestedSource) {
        this.startedAt = startedAt;
        this.requestedSource = requestedSource;
    }
}
