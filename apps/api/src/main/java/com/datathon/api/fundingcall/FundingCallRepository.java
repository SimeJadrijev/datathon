package com.datathon.api.fundingcall;

import com.datathon.api.source.SourceCode;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FundingCallRepository extends JpaRepository<FundingCall, Long> {

    Optional<FundingCall> findBySourceAndExternalId(SourceCode source, String externalId);

    List<FundingCall> findByStatus(FundingCallStatus status);

    long countBySource(SourceCode source);

    /** Calls the model has not read yet, open and announced ones first. */
    @Query("""
            select c.id from FundingCall c
            where c.extractedByLlm = false and c.hidden = false and c.sourceText is not null and length(c.sourceText) > 300
            order by case when c.status in (com.datathon.api.fundingcall.FundingCallStatus.OPEN,
                                            com.datathon.api.fundingcall.FundingCallStatus.ANNOUNCED)
                          then 0 else 1 end, c.id
            """)
    List<Long> findIdsForExtraction(Pageable pageable);
}
