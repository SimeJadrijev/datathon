package com.datathon.api.association;

import jakarta.persistence.EntityManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class AssociationStore {

    static final int BATCH_SIZE = 1000;

    private final AssociationRepository associationRepository;
    private final EntityManager entityManager;

    @Transactional
    public void replaceAll(List<AssociationRecord> records) {
        entityManager.createNativeQuery("delete from association_area").executeUpdate();
        entityManager.createNativeQuery("delete from association").executeUpdate();
        for (int start = 0; start < records.size(); start += BATCH_SIZE) {
            List<AssociationRecord> batch = records.subList(start, Math.min(records.size(), start + BATCH_SIZE));
            associationRepository.saveAll(batch.stream().map(AssociationRecord::toEntity).toList());
            entityManager.flush();
            entityManager.clear();
        }
    }

    @Transactional(readOnly = true)
    public List<AssociationRecord> exportAll() {
        return associationRepository.findAll().stream().map(AssociationRecord::from).toList();
    }
}
