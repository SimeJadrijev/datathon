package com.datathon.api.regulation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import com.datathon.api.common.FacetCount;
import com.datathon.api.common.FacetCounter;
import com.datathon.api.common.InvalidRequestException;
import com.datathon.api.common.NotFoundException;
import com.datathon.api.common.PageResponse;
import com.datathon.api.fundingcall.Area;
import com.datathon.api.regulation.dto.RegulationFilter;
import com.datathon.api.regulation.dto.RegulationSummary;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RegulationServiceTest {

    @Autowired
    private RegulationRepository regulationRepository;

    @Autowired
    private EntityManager entityManager;

    private RegulationService regulationService;

    @BeforeEach
    void setUp() {
        entityManager.createNativeQuery("TRUNCATE TABLE regulation CASCADE").executeUpdate();
        regulationService = new RegulationService(regulationRepository, new FacetCounter(entityManager));
    }

    @Test
    void lawsFromTheRequestedYearAreReturnedNewestFirst() {
        save("Zakon A", RegulationType.LAW, LocalDate.of(2026, 2, 1));
        save("Zakon B", RegulationType.LAW, LocalDate.of(2026, 9, 1));
        save("Zakon iz 2025.", RegulationType.LAW, LocalDate.of(2025, 12, 31));
        save("Uredba", RegulationType.DECREE, LocalDate.of(2026, 5, 1));

        var result = regulationService.list(filter(null, RegulationType.LAW, null, LocalDate.of(2026, 1, 1), null));

        assertThat(titles(result)).containsExactly("Zakon B", "Zakon A");
    }

    @Test
    void publishedRangeIsInclusiveOnBothEnds() {
        save("Prvi dan", RegulationType.LAW, LocalDate.of(2026, 3, 1));
        save("Zadnji dan", RegulationType.LAW, LocalDate.of(2026, 3, 31));
        save("Izvan raspona", RegulationType.LAW, LocalDate.of(2026, 4, 1));

        var result = regulationService.list(
                filter(null, null, null, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31)));

        assertThat(titles(result)).containsExactlyInAnyOrder("Prvi dan", "Zadnji dan");
    }

    @Test
    void invertedPublishedRangeIsRejected() {
        var inverted = filter(null, null, null, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 4, 1));

        assertThatThrownBy(() -> regulationService.list(inverted)).isInstanceOf(InvalidRequestException.class);
        assertThatThrownBy(() -> regulationService.facets(inverted)).isInstanceOf(InvalidRequestException.class);
    }

    @Test
    void emptyPublicationDatesComeLast() {
        save("Bez datuma", RegulationType.LAW, null);
        save("S datumom", RegulationType.LAW, LocalDate.of(2026, 1, 1));

        var result = regulationService.list(filter(null, null, null, null, null));

        assertThat(titles(result)).containsExactly("S datumom", "Bez datuma");
    }

    @Test
    void searchIgnoresCaseAndAccentsInTitle() {
        save("Zakon o zaštiti okoliša", RegulationType.LAW, LocalDate.of(2026, 1, 1));
        save("Zakon o cestama", RegulationType.LAW, LocalDate.of(2026, 1, 2));

        var result = regulationService.list(filter("ZASTITI OKOLISA", null, null, null, null));

        assertThat(titles(result)).containsExactly("Zakon o zaštiti okoliša");
    }

    @Test
    void areaFilterMatchesRegulationsTaggedWithTheArea() {
        save("Energetski", RegulationType.LAW, LocalDate.of(2026, 1, 1)).getAreas().add(Area.ENERGY);
        save("Prometni", RegulationType.LAW, LocalDate.of(2026, 1, 2)).getAreas().add(Area.TRANSPORT);

        var result = regulationService.list(filter(null, null, Area.ENERGY, null, null));

        assertThat(titles(result)).containsExactly("Energetski");
    }

    @Test
    void listItemTellsWhetherASummaryExists() {
        save("Bez sažetka", RegulationType.LAW, LocalDate.of(2026, 1, 1));
        save("Sa sažetkom", RegulationType.LAW, LocalDate.of(2026, 1, 2)).setSummary("Kratak sažetak.");

        var result = regulationService.list(filter(null, null, null, null, null));

        assertThat(result.content()).extracting(RegulationSummary::title, RegulationSummary::hasSummary)
                .containsExactly(tuple("Sa sažetkom", true), tuple("Bez sažetka", false));
    }

    @Test
    void detailOfMissingRegulationIsNotFound() {
        assertThatThrownBy(() -> regulationService.getById(-1L)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void facetsIgnoreTheirOwnFilterButRespectTheOthers() {
        save("Energetski zakon", RegulationType.LAW, LocalDate.of(2026, 1, 1)).getAreas().add(Area.ENERGY);
        save("Energetska uredba", RegulationType.DECREE, LocalDate.of(2026, 1, 2)).getAreas().add(Area.ENERGY);
        save("Prometni zakon", RegulationType.LAW, LocalDate.of(2026, 1, 3)).getAreas().add(Area.TRANSPORT);
        entityManager.flush();

        var facets = regulationService.facets(filter(null, RegulationType.LAW, Area.ENERGY, null, null));

        assertThat(facets.type()).extracting(FacetCount::value, FacetCount::count)
                .containsExactlyInAnyOrder(tuple("LAW", 1L), tuple("DECREE", 1L));
        assertThat(facets.area()).extracting(FacetCount::value, FacetCount::count)
                .containsExactlyInAnyOrder(tuple("ENERGY", 1L), tuple("TRANSPORT", 1L));
    }

    private Regulation save(String title, RegulationType type, LocalDate publishedAt) {
        var regulation = new Regulation("nn-" + title, title);
        regulation.setType(type);
        regulation.setPublishedAt(publishedAt);
        return regulationRepository.save(regulation);
    }

    private static RegulationFilter filter(String q, RegulationType type, Area area, LocalDate from, LocalDate to) {
        return new RegulationFilter(q, type, area, from, to, null, null);
    }

    private static List<String> titles(PageResponse<RegulationSummary> result) {
        return result.content().stream().map(RegulationSummary::title).toList();
    }
}
