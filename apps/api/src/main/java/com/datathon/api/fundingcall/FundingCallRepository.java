package com.datathon.api.fundingcall;

import com.datathon.api.source.SourceCode;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FundingCallRepository extends JpaRepository<FundingCall, Long> {

    Optional<FundingCall> findBySourceAndExternalId(SourceCode source, String externalId);

    List<FundingCall> findByStatus(FundingCallStatus status);

    long countBySource(SourceCode source);
}
