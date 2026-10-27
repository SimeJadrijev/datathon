package com.datathon.api.ai;

import java.math.BigDecimal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LlmUsageRepository extends JpaRepository<LlmUsage, Long> {

    @Query("select coalesce(sum(u.costUsd), 0) from LlmUsage u")
    BigDecimal sumCostUsd();
}
