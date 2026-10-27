package com.datathon.api.regulation;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegulationRepository extends JpaRepository<Regulation, Long> {

    Optional<Regulation> findByExternalId(String externalId);
}
