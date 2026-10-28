package com.datathon.api.ai;

import java.math.BigDecimal;

public record ExtractionRunResult(int extracted, int failed, int skipped, BigDecimal costUsd) {
}
