package com.datathon.api.regulation;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RegulationRepository extends JpaRepository<Regulation, Long>, JpaSpecificationExecutor<Regulation> {

    Optional<Regulation> findByExternalId(String externalId);
}
