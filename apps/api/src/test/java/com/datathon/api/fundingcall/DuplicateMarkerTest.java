package com.datathon.api.fundingcall;

import static org.assertj.core.api.Assertions.assertThat;

import com.datathon.api.source.SourceCode;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(DuplicateMarker.class)
class DuplicateMarkerTest {

    private static final String TITLE = "Objavljen Treći natječaj za intervenciju 73.02 Obnova poljoprivrednog potencijala";

    @Autowired
    private DuplicateMarker duplicateMarker;

    @Autowired
    private FundingCallRepository fundingCallRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        entityManager.createNativeQuery("TRUNCATE TABLE funding_call CASCADE").executeUpdate();
    }

    @Test
    void ministryRepostOfAnAgencyCallPointsToTheAgencyCall() {
        FundingCall news = save(SourceCode.GOV_HR_NEWS, "poljoprivreda.gov.hr:8474", TITLE, LocalDate.of(2026, 10, 20));
        FundingCall agency = save(SourceCode.APPRRR, "43738", "Objavljen treći Natječaj za intervenciju 73.02. "
                + "„Obnova poljoprivrednog potencijala“", LocalDate.of(2026, 10, 21));

        duplicateMarker.markDuplicates();

        assertThat(news.getDuplicateOf()).isEqualTo(agency.getId());
        assertThat(agency.getDuplicateOf()).isNull();
    }

    @Test
    void sameTitleFromTheSameOriginOrAnotherYearIsNotADuplicate() {
        FundingCall first = save(SourceCode.EU_FUNDS_NPOO, "107", "Uspostava reciklažnih dvorišta", null);
        FundingCall second = save(SourceCode.EU_FUNDS_NPOO, "45", "Uspostava reciklažnih dvorišta", null);
        FundingCall city = save(SourceCode.LOCAL_GOVERNMENT, "split.hr:1", "Javni poziv za udruge", LocalDate.of(2025, 1, 10));
        FundingCall otherCity = save(SourceCode.LOCAL_GOVERNMENT, "zadar.hr:1", "Javni poziv za udruge",
                LocalDate.of(2026, 1, 10));

        duplicateMarker.markDuplicates();

        assertThat(first.getDuplicateOf()).isNull();
        assertThat(second.getDuplicateOf()).isNull();
        assertThat(city.getDuplicateOf()).isNull();
        assertThat(otherCity.getDuplicateOf()).isNull();
    }

    @Test
    void differentDeadlinesMeanDifferentCalls() {
        FundingCall agency = save(SourceCode.APPRRR, "1", TITLE, null);
        agency.setDeadline(LocalDate.of(2026, 11, 30));
        FundingCall news = save(SourceCode.GOV_HR_NEWS, "poljoprivreda.gov.hr:1", TITLE, null);
        news.setDeadline(LocalDate.of(2027, 3, 1));

        duplicateMarker.markDuplicates();

        assertThat(news.getDuplicateOf()).isNull();
    }

    @Test
    void samePageStoredUnderTwoIdsOfOneSourceIsADuplicate() {
        String url = "https://www.smz.hr/natj/javni?view=article&id=12207:javni-poziv&catid=107";
        FundingCall first = save(SourceCode.LOCAL_GOVERNMENT, "smz.hr:a3158d33-141", "Javni poziv u kulturi", null);
        first.setUrl(url);
        FundingCall second = save(SourceCode.LOCAL_GOVERNMENT, "smz.hr:d7d88e1e-165", "Javni poziv u kulturi", null);
        second.setUrl(url);

        duplicateMarker.markDuplicates();

        assertThat(first.getDuplicateOf()).isNull();
        assertThat(second.getDuplicateOf()).isEqualTo(first.getId());
    }

    private FundingCall save(SourceCode source, String externalId, String title, LocalDate publishedAt) {
        FundingCall call = new FundingCall(source, externalId, title);
        call.setPublishedAt(publishedAt);
        return fundingCallRepository.save(call);
    }
}
