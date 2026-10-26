package com.datathon.api.ai;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "llm_usage")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LlmUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String model;

    private int inputTokens;

    private int outputTokens;

    @Column(precision = 12, scale = 6)
    private BigDecimal costUsd;

    @Column(length = 50)
    private String purpose;

    @Column(nullable = false)
    private Instant createdAt;

    public LlmUsage(String model, String purpose, Instant createdAt) {
        this.model = model;
        this.purpose = purpose;
        this.createdAt = createdAt;
    }
}
