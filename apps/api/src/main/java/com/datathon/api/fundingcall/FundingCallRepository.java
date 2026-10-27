package com.datathon.api.fundingcall;

import com.datathon.api.source.SourceCode;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface FundingCallRepository extends JpaRepository<FundingCall, Long>, JpaSpecificationExecutor<FundingCall> {

    Optional<FundingCall> findBySourceAndExternalId(SourceCode source, String externalId);

    List<FundingCall> findByStatus(FundingCallStatus status);

    long countBySource(SourceCode source);

    @Query("select distinct county from FundingCall fundingCall join fundingCall.counties county")
    List<String> findDistinctCounties();
}
