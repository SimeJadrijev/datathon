package com.datathon.api.fundingcall;

import com.datathon.api.source.SourceCode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FundingCallRepository extends JpaRepository<FundingCall, Long>, JpaSpecificationExecutor<FundingCall> {

    Optional<FundingCall> findBySourceAndExternalId(SourceCode source, String externalId);

    List<FundingCall> findByStatus(FundingCallStatus status);

    long countBySource(SourceCode source);

    @Query("select distinct county from FundingCall fundingCall join fundingCall.counties county")
    List<String> findDistinctCounties();

    /** Sources keep listing a call as open after its deadline, so the stored status is corrected from the deadline. */
    @Modifying
    @Query("""
            update FundingCall c set c.status = com.datathon.api.fundingcall.FundingCallStatus.CLOSED
            where c.status in (com.datathon.api.fundingcall.FundingCallStatus.OPEN,
                               com.datathon.api.fundingcall.FundingCallStatus.ANNOUNCED)
              and c.deadline < :today
            """)
    int closeExpired(@Param("today") LocalDate today);

    List<FundingCall> findByStatusAndDeadlineIsNull(FundingCallStatus status);

    /** Calls the model has not read yet, open and announced ones first. */
    @Query("""
            select c.id from FundingCall c
            where c.extractedByLlm = false and c.hidden = false and c.sourceText is not null and length(c.sourceText) > 300
            order by case when c.status in (com.datathon.api.fundingcall.FundingCallStatus.OPEN,
                                            com.datathon.api.fundingcall.FundingCallStatus.ANNOUNCED)
                          then 0 else 1 end, c.id
            """)
    List<Long> findIdsForExtraction(Pageable pageable);

    /**
     * Calls with enough text and no eligibility questions yet, open and announced ones first. Closed and expired
     * calls are left out: nobody can apply, so questions would only cost money and invite a misleading answer.
     */
    @Query("""
            select c.id from FundingCall c
            where c.eligibilityQuestions is null and c.hidden = false
              and c.sourceText is not null and length(c.sourceText) > 300
              and (c.status is null or c.status <> com.datathon.api.fundingcall.FundingCallStatus.CLOSED)
              and (c.deadline is null or c.deadline >= :today)
            order by case when c.status in (com.datathon.api.fundingcall.FundingCallStatus.OPEN,
                                            com.datathon.api.fundingcall.FundingCallStatus.ANNOUNCED)
                          then 0 else 1 end, c.id
            """)
    List<Long> findIdsForQuestions(@Param("today") LocalDate today, Pageable pageable);

    @Query("""
            select c.id from FundingCall c
            where c.legalReferences is empty and c.sourceText is not null
              and (c.sourceText like '%Narodn%' or c.sourceText like '%NN %')
            """)
    List<Long> findIdsWithGazetteTextAndNoLegalReferences();

    @Query("""
            select distinct c from FundingCall c join c.legalReferences reference
            where reference.gazetteNumber = :gazetteNumber and c.hidden = false and c.duplicateOf is null
            """)
    List<FundingCall> findVisibleCitingGazetteNumber(@Param("gazetteNumber") String gazetteNumber);

    @Query("select distinct reference from FundingCall c join c.legalReferences reference where reference.citedTitle is not null")
    List<LegalReference> findDistinctTitledLegalReferences();

    @Modifying
    @Query(value = """
            update funding_call
            set first_seen_at = coalesce(published_at::timestamp at time zone 'Europe/Zagreb', fetched_at, now())
            where first_seen_at is null
            """, nativeQuery = true)
    int fillMissingFirstSeenAt();
}
