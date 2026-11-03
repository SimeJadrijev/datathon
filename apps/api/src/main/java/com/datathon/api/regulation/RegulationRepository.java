package com.datathon.api.regulation;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface RegulationRepository extends JpaRepository<Regulation, Long>, JpaSpecificationExecutor<Regulation> {

    Optional<Regulation> findByExternalId(String externalId);

    List<Regulation> findByGazetteNumberIn(Collection<String> gazetteNumbers);

    /** Acts without a summary: laws first, newest first. */
    @Query("""
            select r.id from Regulation r
            where r.summary is null
            order by case when r.type = com.datathon.api.regulation.RegulationType.LAW then 0 else 1 end,
                     r.publishedAt desc, r.id desc
            """)
    List<Long> findIdsForSummary(Pageable pageable);
}
