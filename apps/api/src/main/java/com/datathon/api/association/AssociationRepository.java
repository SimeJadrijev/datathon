package com.datathon.api.association;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssociationRepository extends JpaRepository<Association, Long> {

    Optional<Association> findByOib(String oib);
}
