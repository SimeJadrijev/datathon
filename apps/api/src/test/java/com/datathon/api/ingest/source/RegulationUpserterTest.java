package com.datathon.api.ingest.source;

import static org.assertj.core.api.Assertions.assertThat;

import com.datathon.api.fundingcall.Area;
import com.datathon.api.regulation.Regulation;
import com.datathon.api.regulation.RegulationRepository;
import com.datathon.api.regulation.RegulationType;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(RegulationUpserter.class)
class RegulationUpserterTest {

    private static final String ID = "2026-1-1";

    @Autowired
    private RegulationUpserter upserter;

    @Autowired
    private RegulationRepository regulationRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void clean() {
        entityManager.createNativeQuery("TRUNCATE TABLE regulation CASCADE").executeUpdate();
    }

    @Test
    void listValuesDoNotOverwriteWhatEliAlreadyCorrected() {
        upserter.upsertAll(List.of(listed(RegulationType.OTHER, LocalDate.of(2026, 1, 5))));
        upserter.upsertAll(List.of(listed(RegulationType.OTHER, LocalDate.of(2026, 1, 5)).toBuilder()
                .type(RegulationType.LAW).publishedAt(LocalDate.of(2026, 1, 3))
                .enactedAt(LocalDate.of(2025, 12, 20)).eliMetadata(true).build()));

        upserter.upsertAll(List.of(listed(RegulationType.OTHER, LocalDate.of(2026, 1, 5))));

        Regulation stored = regulationRepository.findByExternalId(ID).orElseThrow();
        assertThat(stored.getType()).isEqualTo(RegulationType.LAW);
        assertThat(stored.getPublishedAt()).isEqualTo(LocalDate.of(2026, 1, 3));
        assertThat(stored.getEnactedAt()).isEqualTo(LocalDate.of(2025, 12, 20));
        assertThat(stored.isEliMetadata()).isTrue();
    }

    @Test
    void listValuesStillUpdateAnActWithoutEliMetadata() {
        upserter.upsertAll(List.of(listed(RegulationType.OTHER, LocalDate.of(2026, 1, 5))));

        upserter.upsertAll(List.of(listed(RegulationType.DECISION, LocalDate.of(2026, 1, 6))));

        Regulation stored = regulationRepository.findByExternalId(ID).orElseThrow();
        assertThat(stored.getType()).isEqualTo(RegulationType.DECISION);
        assertThat(stored.getPublishedAt()).isEqualTo(LocalDate.of(2026, 1, 6));
        assertThat(stored.isEliMetadata()).isFalse();
    }

    @Test
    void refreshKeepsAreasAddedEarlier() {
        upserter.upsertAll(List.of(listed(RegulationType.LAW, LocalDate.of(2026, 1, 5))));
        Regulation stored = regulationRepository.findByExternalId(ID).orElseThrow();
        stored.getAreas().add(Area.HEALTH);
        regulationRepository.saveAndFlush(stored);

        upserter.upsertAll(List.of(listed(RegulationType.LAW, LocalDate.of(2026, 1, 5)).toBuilder()
                .areas(Set.of(Area.ENERGY)).build()));

        assertThat(regulationRepository.findByExternalId(ID).orElseThrow().getAreas()).containsExactly(Area.HEALTH);
    }

    private static ParsedRegulation listed(RegulationType type, LocalDate publishedAt) {
        return ParsedRegulation.builder()
                .externalId(ID).title("Zakon o testu").type(type).gazetteNumber("1/2026").documentNumber(1)
                .publishedAt(publishedAt).url("https://narodne-novine.nn.hr/a").build();
    }
}
