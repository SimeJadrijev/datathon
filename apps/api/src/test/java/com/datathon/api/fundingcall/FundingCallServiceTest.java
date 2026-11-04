package com.datathon.api.fundingcall;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import com.datathon.api.common.FacetCount;
import com.datathon.api.common.FacetCounter;
import com.datathon.api.common.NotFoundException;
import com.datathon.api.common.PageResponse;
import com.datathon.api.fundingcall.dto.FundingCallFilter;
import com.datathon.api.fundingcall.dto.FundingCallRecord;
import com.datathon.api.fundingcall.dto.FundingCallSort;
import com.datathon.api.fundingcall.dto.FundingCallStatusFilter;
import com.datathon.api.fundingcall.dto.FundingCallSummary;
import com.datathon.api.legalbasis.LegalBasisService;
import com.datathon.api.regulation.RegulationRepository;
import com.datathon.api.source.SourceCatalog;
import com.datathon.api.source.SourceCode;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import tools.jackson.databind.json.JsonMapper;

@DataJpaTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class FundingCallServiceTest {

    @Autowired
    private FundingCallRepository fundingCallRepository;

    @Autowired
    private RegulationRepository regulationRepository;

    @Autowired
    private EntityManager entityManager;

    private FundingCallService fundingCallService;

    @BeforeEach
    void setUp() {
        entityManager.createNativeQuery("TRUNCATE TABLE funding_call CASCADE").executeUpdate();
        fundingCallService = new FundingCallService(fundingCallRepository, new SourceCatalog(JsonMapper.builder().build()),
                new FacetCounter(entityManager), new LegalBasisService(regulationRepository, fundingCallRepository));
    }

    @Test
    void closeExpiredClosesOpenAndAnnouncedCallsWhoseDeadlineHasPassed() {
        LocalDate today = LocalDate.of(2026, 10, 30);
        save("Istekao", FundingCallStatus.OPEN, TargetGroup.COMPANY).setDeadline(today.minusDays(1));
        save("Ističe danas", FundingCallStatus.OPEN, TargetGroup.COMPANY).setDeadline(today);
        save("Bez roka", FundingCallStatus.OPEN, TargetGroup.COMPANY);
        save("Najavljen", FundingCallStatus.ANNOUNCED, TargetGroup.COMPANY).setDeadline(today.minusDays(1));
        entityManager.flush();

        int closed = fundingCallRepository.closeExpired(today);
        entityManager.clear();

        assertThat(closed).isEqualTo(2);
        assertThat(fundingCallRepository.findByStatus(FundingCallStatus.CLOSED))
                .extracting(FundingCall::getTitle).containsExactlyInAnyOrder("Istekao", "Najavljen");
    }

    @Test
    void openCallsWithoutDeadlineThatAreSuspendedOrForAPastYearAreClosed() {
        save("Javni poziv za udruge za 2022. godinu", FundingCallStatus.OPEN, TargetGroup.ASSOCIATION);
        save("OBUSTAVA - Javni natječaj za donacije", FundingCallStatus.OPEN, TargetGroup.ASSOCIATION);
        save("Javni poziv za udruge za " + LocalDate.now().getYear() + ". godinu", FundingCallStatus.OPEN,
                TargetGroup.ASSOCIATION);
        entityManager.flush();

        new ExpiredFundingCallCloser(fundingCallRepository).closeExpired();
        entityManager.flush();
        entityManager.clear();

        assertThat(fundingCallRepository.findByStatus(FundingCallStatus.OPEN)).extracting(FundingCall::getTitle)
                .containsExactly("Javni poziv za udruge za " + LocalDate.now().getYear() + ". godinu");
    }

    @Test
    void blankCountyIsNoFilterAndCountySpellingsWithoutDiacriticsMatch() {
        save("Nacionalni", FundingCallStatus.OPEN, TargetGroup.COMPANY).getCounties().add("ALL");
        save("Međimurski", FundingCallStatus.OPEN, TargetGroup.COMPANY).getCounties().add("Medimurska");
        entityManager.flush();

        var blank = fundingCallService.list(filter(FundingCallStatusFilter.OPEN, null, " ", null, null));
        var county = fundingCallService.list(filter(FundingCallStatusFilter.OPEN, null, "Međimurska županija", null, null));

        assertThat(titles(blank)).containsExactlyInAnyOrder("Nacionalni", "Međimurski");
        assertThat(titles(county)).containsExactlyInAnyOrder("Nacionalni", "Međimurski");
    }

    @Test
    void returnsOnlyOpenCallsForTheRequestedTargetGroup() {
        save("Otvoren za tvrtke", FundingCallStatus.OPEN, TargetGroup.COMPANY);
        save("Otvoren za studente", FundingCallStatus.OPEN, TargetGroup.STUDENT);
        save("Zatvoren za tvrtke", FundingCallStatus.CLOSED, TargetGroup.COMPANY);

        var result = fundingCallService.list(filter(FundingCallStatusFilter.OPEN, TargetGroup.COMPANY, null, null, null));

        assertThat(titles(result)).containsExactly("Otvoren za tvrtke");
        assertThat(result.totalElements()).isEqualTo(1);
    }

    @Test
    void statusDefaultsToOpen() {
        save("Otvoren", FundingCallStatus.OPEN, TargetGroup.COMPANY);
        save("Zatvoren", FundingCallStatus.CLOSED, TargetGroup.COMPANY);
        save("Bez statusa", null, TargetGroup.COMPANY);

        var result = fundingCallService.list(filter(null, null, null, null, null));

        assertThat(titles(result)).containsExactly("Otvoren");
    }

    @Test
    void unknownStatusFindsCallsWithoutStatus() {
        save("Otvoren", FundingCallStatus.OPEN, TargetGroup.COMPANY);
        save("Bez statusa", null, TargetGroup.COMPANY);

        var result = fundingCallService.list(filter(FundingCallStatusFilter.UNKNOWN, null, null, null, null));

        assertThat(titles(result)).containsExactly("Bez statusa");
    }

    @Test
    void hiddenAndDuplicateCallsAreNeverReturned() {
        save("Vidljiv", FundingCallStatus.OPEN, TargetGroup.COMPANY);
        FundingCall hidden = save("Skriven", FundingCallStatus.OPEN, TargetGroup.COMPANY);
        hidden.setHidden(true);
        FundingCall duplicate = save("Duplikat", FundingCallStatus.OPEN, TargetGroup.COMPANY);
        duplicate.setDuplicateOf(hidden.getId());

        var result = fundingCallService.list(filter(null, null, null, null, null));

        assertThat(titles(result)).containsExactly("Vidljiv");
    }

    @Test
    void sortsByDeadlineWithEmptyDeadlinesLast() {
        save("Bez roka", FundingCallStatus.OPEN, TargetGroup.COMPANY);
        save("Kasniji rok", FundingCallStatus.OPEN, TargetGroup.COMPANY).setDeadline(LocalDate.of(2026, 12, 1));
        save("Raniji rok", FundingCallStatus.OPEN, TargetGroup.COMPANY).setDeadline(LocalDate.of(2026, 11, 5));

        var result = fundingCallService.list(filter(null, null, null, null, FundingCallSort.DEADLINE));

        assertThat(titles(result)).containsExactly("Raniji rok", "Kasniji rok", "Bez roka");
    }

    @Test
    void sortsByPublishedDateNewestFirst() {
        save("Stariji", FundingCallStatus.OPEN, TargetGroup.COMPANY).setPublishedAt(LocalDate.of(2026, 1, 10));
        save("Noviji", FundingCallStatus.OPEN, TargetGroup.COMPANY).setPublishedAt(LocalDate.of(2026, 9, 10));
        save("Bez datuma", FundingCallStatus.OPEN, TargetGroup.COMPANY);

        var result = fundingCallService.list(filter(null, null, null, null, FundingCallSort.PUBLISHED_AT));

        assertThat(titles(result)).containsExactly("Noviji", "Stariji", "Bez datuma");
    }

    @Test
    void countyFilterMatchesNormalizedNamesAndCallsForTheWholeCountry() {
        save("Istarski", FundingCallStatus.OPEN, TargetGroup.COMPANY).getCounties().add("Istarska");
        save("Cijela država", FundingCallStatus.OPEN, TargetGroup.COMPANY).getCounties().add("ALL");
        save("Područje cijele Hrvatske", FundingCallStatus.OPEN, TargetGroup.COMPANY).getCounties().add("Područje cijele Hrvatske");
        save("Primorski", FundingCallStatus.OPEN, TargetGroup.COMPANY).getCounties().add("Primorsko-goranska");

        var result = fundingCallService.list(filter(null, null, "Istarska županija", null, FundingCallSort.PUBLISHED_AT));

        assertThat(titles(result)).containsExactlyInAnyOrder("Istarski", "Cijela država", "Područje cijele Hrvatske");
    }

    @Test
    void searchIgnoresCaseAndAccentsInTitleAndProvider() {
        save("Stipendija Kraš", FundingCallStatus.OPEN, TargetGroup.STUDENT);
        FundingCall byProvider = save("Drugi poziv", FundingCallStatus.OPEN, TargetGroup.STUDENT);
        byProvider.setProvider("Šibensko-kninska županija");
        save("Nevezano", FundingCallStatus.OPEN, TargetGroup.STUDENT);

        assertThat(titles(fundingCallService.list(filter(null, null, null, "KRAS", null)))).containsExactly("Stipendija Kraš");
        assertThat(titles(fundingCallService.list(filter(null, null, null, "sibensko", null)))).containsExactly("Drugi poziv");
    }

    @Test
    void searchTreatsPercentAndUnderscoreAsPlainCharacters() {
        save("Sufinanciranje 50% troška", FundingCallStatus.OPEN, TargetGroup.COMPANY);
        save("Sufinanciranje opreme", FundingCallStatus.OPEN, TargetGroup.COMPANY);

        assertThat(titles(fundingCallService.list(filter(null, null, null, "50%", null)))).containsExactly("Sufinanciranje 50% troška");
        assertThat(titles(fundingCallService.list(filter(null, null, null, "_", null)))).isEmpty();
    }

    @Test
    void deadlineBeforeIsInclusive() {
        save("Na rubu", FundingCallStatus.OPEN, TargetGroup.COMPANY).setDeadline(LocalDate.of(2026, 11, 30));
        save("Kasnije", FundingCallStatus.OPEN, TargetGroup.COMPANY).setDeadline(LocalDate.of(2026, 12, 1));
        save("Bez roka", FundingCallStatus.OPEN, TargetGroup.COMPANY);
        var filter = new FundingCallFilter(null, null, null, null, null, null, null, LocalDate.of(2026, 11, 30), null, null, null);

        assertThat(titles(fundingCallService.list(filter))).containsExactly("Na rubu");
    }

    @Test
    void detailOfHiddenOrMissingCallIsNotFound() {
        FundingCall hidden = save("Skriven", FundingCallStatus.OPEN, TargetGroup.COMPANY);
        hidden.setHidden(true);
        entityManager.flush();

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> fundingCallService.getById(hidden.getId()))
                .isInstanceOf(NotFoundException.class);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> fundingCallService.getById(-1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void detailDoesNotExposeSourceText() {
        FundingCall call = save("S tekstom", FundingCallStatus.OPEN, TargetGroup.COMPANY);
        call.setSourceText("interni tekst izvora");
        entityManager.flush();

        var detail = fundingCallService.getById(call.getId());

        assertThat(detail.toString()).doesNotContain("interni tekst izvora");
        assertThat(detail.source().code()).isEqualTo(SourceCode.FZOEU);
    }

    @Test
    void listDoesNotRunOneQueryPerRow() {
        for (int i = 0; i < 15; i++) {
            FundingCall call = save("Poziv " + i, FundingCallStatus.OPEN, TargetGroup.COMPANY);
            call.getAreas().add(Area.ENERGY);
            call.getCounties().add("ALL");
        }
        entityManager.flush();
        entityManager.clear();
        var statistics = entityManager.getEntityManagerFactory().unwrap(SessionFactory.class).getStatistics();
        statistics.clear();

        var result = fundingCallService.list(filter(null, null, null, null, null));

        assertThat(result.content()).hasSize(15);
        assertThat(statistics.getPrepareStatementCount()).isLessThanOrEqualTo(6);
    }

    @Test
    void statusFacetSumsToTheNumberOfVisibleCalls() {
        save("Otvoren", FundingCallStatus.OPEN, TargetGroup.COMPANY);
        save("Zatvoren", FundingCallStatus.CLOSED, TargetGroup.STUDENT);
        save("Bez statusa", null, TargetGroup.COMPANY);
        FundingCall hidden = save("Skriven", FundingCallStatus.OPEN, TargetGroup.COMPANY);
        hidden.setHidden(true);
        FundingCall duplicate = save("Duplikat", FundingCallStatus.OPEN, TargetGroup.COMPANY);
        duplicate.setDuplicateOf(hidden.getId());
        entityManager.flush();

        var facets = fundingCallService.facets(noFilters());

        assertThat(facets.status()).extracting(FacetCount::value, FacetCount::count)
                .containsExactlyInAnyOrder(tuple("OPEN", 1L), tuple("CLOSED", 1L), tuple("UNKNOWN", 1L));
        assertThat(facets.status()).extracting(FacetCount::count).containsOnly(1L);
        assertThat(facets.source()).extracting(FacetCount::count).containsExactly(1L);
    }

    @Test
    void facetIgnoresItsOwnFilterButRespectsTheOthers() {
        save("Student energija", FundingCallStatus.OPEN, TargetGroup.STUDENT).getAreas().add(Area.ENERGY);
        save("Student obrazovanje", FundingCallStatus.OPEN, TargetGroup.STUDENT).getAreas().add(Area.EDUCATION);
        save("Tvrtka promet", FundingCallStatus.OPEN, TargetGroup.COMPANY).getAreas().add(Area.TRANSPORT);
        entityManager.flush();
        var filter = new FundingCallFilter(null, null, TargetGroup.STUDENT, null, null, null, null, null, null, null, null);

        var facets = fundingCallService.facets(filter);

        assertThat(facets.area()).extracting(FacetCount::value).containsExactlyInAnyOrder("ENERGY", "EDUCATION");
        assertThat(facets.targetGroup()).extracting(FacetCount::value, FacetCount::count)
                .containsExactlyInAnyOrder(tuple("STUDENT", 2L), tuple("COMPANY", 1L));
    }

    @Test
    void countyFacetMergesWholeCountryValuesIntoAll() {
        save("Istarski", FundingCallStatus.OPEN, TargetGroup.COMPANY).getCounties().add("Istarska");
        save("Cijela država", FundingCallStatus.OPEN, TargetGroup.COMPANY).getCounties().add("ALL");
        save("Cijela Hrvatska", FundingCallStatus.OPEN, TargetGroup.COMPANY).getCounties().add("Područje cijele Hrvatske");
        entityManager.flush();

        var facets = fundingCallService.facets(noFilters());

        assertThat(facets.county()).extracting(FacetCount::value, FacetCount::count)
                .containsExactly(tuple("Istarska", 3L), tuple("ALL", 2L));
    }

    @Test
    void countyFacetCountsWholeCountryCallsLikeTheCountyFilterDoes() {
        save("Istarski", FundingCallStatus.OPEN, TargetGroup.COMPANY).getCounties().add("Istarska");
        save("Cijela država", FundingCallStatus.OPEN, TargetGroup.COMPANY).getCounties().add("ALL");
        save("Istra i država", FundingCallStatus.OPEN, TargetGroup.COMPANY).getCounties().addAll(List.of("Istarska", "ALL"));
        save("Primorski", FundingCallStatus.OPEN, TargetGroup.COMPANY).getCounties().add("Primorsko-goranska");
        entityManager.flush();

        var facets = fundingCallService.facets(noFilters());
        long istrianCalls = fundingCallService.list(filter(null, null, "Istarska", null, null)).totalElements();

        assertThat(facets.county()).extracting(FacetCount::value, FacetCount::count)
                .containsExactlyInAnyOrder(tuple("Istarska", 3L), tuple("ALL", 2L), tuple("Primorsko-goranska", 3L));
        assertThat(istrianCalls).isEqualTo(3L);
    }

    @Test
    void countyFacetMergesSpellingsOfTheSameCountyAndCountsACallOnce() {
        save("Prvi", FundingCallStatus.OPEN, TargetGroup.COMPANY).getCounties().add("Istarska");
        save("Drugi", FundingCallStatus.OPEN, TargetGroup.COMPANY).getCounties().add("Istarska županija");
        save("Treći", FundingCallStatus.OPEN, TargetGroup.COMPANY).getCounties().add("Istarska županija");
        FundingCall bothWholeCountrySpellings = save("Cijela", FundingCallStatus.OPEN, TargetGroup.COMPANY);
        bothWholeCountrySpellings.getCounties().addAll(List.of("ALL", "Područje cijele Hrvatske"));
        entityManager.flush();

        var facets = fundingCallService.facets(noFilters());

        assertThat(facets.county()).extracting(FacetCount::value, FacetCount::count)
                .containsExactlyInAnyOrder(tuple("ALL", 1L), tuple("Istarska županija", 4L));
    }

    @Test
    void onlyTheStatusFacetReportsCallsWithoutAValue() {
        save("Bez statusa i tipa", null, TargetGroup.COMPANY);
        save("S tipom", FundingCallStatus.OPEN, TargetGroup.COMPANY).setType(FundingCallType.GRANT);
        save("Bez tipa", FundingCallStatus.OPEN, TargetGroup.COMPANY);
        entityManager.flush();

        var facets = fundingCallService.facets(noFilters());

        assertThat(facets.status()).extracting(FacetCount::value).contains("UNKNOWN");
        assertThat(facets.type()).extracting(FacetCount::value, FacetCount::count).containsExactly(tuple("GRANT", 1L));
    }

    @Test
    void facetsAreSortedByCountDescending() {
        save("A", FundingCallStatus.OPEN, TargetGroup.COMPANY);
        save("B", FundingCallStatus.CLOSED, TargetGroup.COMPANY);
        save("C", FundingCallStatus.CLOSED, TargetGroup.COMPANY);
        entityManager.flush();

        var facets = fundingCallService.facets(noFilters());

        assertThat(facets.status()).extracting(FacetCount::value).containsExactly("CLOSED", "OPEN");
    }

    private static FundingCallFilter noFilters() {
        return new FundingCallFilter(null, null, null, null, null, null, null, null, null, null, null);
    }

    @Test
    void exportHasOnlyVisibleMatchingCallsWithTheirOriginAndLegalBasis() {
        FundingCall open = save("Otvoreni", FundingCallStatus.OPEN, TargetGroup.COMPANY);
        open.setUrl("https://www.fzoeu.hr/poziv/1");
        open.getLegalReferences().addAll(LegalReference.fromText("Zakona o udrugama (Narodne novine, broj 74/14)"));
        save("Skriveni", FundingCallStatus.OPEN, TargetGroup.COMPANY).setHidden(true);
        save("Zatvoreni", FundingCallStatus.CLOSED, TargetGroup.COMPANY);
        entityManager.flush();

        var export = fundingCallService.export(filter(FundingCallStatusFilter.OPEN, null, null, null, null));

        assertThat(export.count()).isEqualTo(1);
        var record = export.records().getFirst();
        assertThat(record.id()).isEqualTo("https://www.fzoeu.hr/poziv/1");
        assertThat(record.origin().sourceCode()).isEqualTo("FZOEU");
        assertThat(record.amounts().currency()).isEqualTo("EUR");
        assertThat(record.legalBasis()).extracting(FundingCallRecord.LegalAct::gazetteIssueUri)
                .containsExactly("https://narodne-novine.nn.hr/eli/sluzbeni/2014/74");
    }

    private FundingCall save(String title, FundingCallStatus status, TargetGroup targetGroup) {
        var call = new FundingCall(SourceCode.FZOEU, "id-" + title, title);
        call.setStatus(status);
        call.getTargetGroups().add(targetGroup);
        return fundingCallRepository.save(call);
    }

    private static FundingCallFilter filter(FundingCallStatusFilter status, TargetGroup targetGroup, String county,
                                            String q, FundingCallSort sort) {
        return new FundingCallFilter(q, status, targetGroup, null, county, null, null, null, null, null, sort);
    }

    private static List<String> titles(PageResponse<FundingCallSummary> result) {
        return result.content().stream().map(FundingCallSummary::title).toList();
    }
}
