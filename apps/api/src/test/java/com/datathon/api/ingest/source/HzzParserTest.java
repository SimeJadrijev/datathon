package com.datathon.api.ingest.source;

import static com.datathon.api.ingest.source.SourceFixtures.json;
import static org.assertj.core.api.Assertions.assertThat;

import com.datathon.api.fundingcall.FundingCall;
import com.datathon.api.fundingcall.FundingCallStatus;
import com.datathon.api.fundingcall.FundingCallType;
import com.datathon.api.fundingcall.TargetGroup;
import com.datathon.api.ingest.RawItem;
import com.datathon.api.ingest.extract.ExtractedFields;
import com.datathon.api.ingest.extract.RuleExtractor;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HzzParserTest {

    private List<RawItem> items;

    @BeforeEach
    void parseFixture() {
        Map<Integer, String> years = HzzParser.termNames(json("hzz/years.json"));
        Map<Integer, String> categories = HzzParser.termNames(json("hzz/categories.json"));
        items = HzzParser.parseMeasures(json("hzz/measures.json"), years, categories);
    }

    @Test
    void parsesMeasure() {
        RawItem employerMeasure = items.getFirst();

        assertThat(employerMeasure.externalId()).isEqualTo("5739");
        assertThat(employerMeasure.url()).startsWith("https://mjere.hzz.hr/mjere/");
        assertThat(employerMeasure.title()).startsWith("Potpora za očuvanje radnih mjesta");
        assertThat(employerMeasure.provider()).isEqualTo("Hrvatski zavod za zapošljavanje");
        assertThat(employerMeasure.publishedAt()).isEqualTo(LocalDate.of(2026, 9, 21));
        assertThat(employerMeasure.typeHint()).isEqualTo(FundingCallType.SUBSIDY);
        assertThat(employerMeasure.countiesHint()).containsExactly(FundingCall.ALL_COUNTIES);
        assertThat(employerMeasure.categories()).contains("Poslodavac", "Zaposlen");
        assertThat(employerMeasure.text()).isNotBlank();
        assertThat(employerMeasure.attachments()).isNotEmpty();
    }

    @Test
    void measureTaggedAsActiveIsOpenAndOlderOnesAreClosed() {
        assertThat(items).extracting(RawItem::statusHint)
                .containsExactly(FundingCallStatus.OPEN, FundingCallStatus.CLOSED, FundingCallStatus.CLOSED);
    }

    @Test
    void employerCategoryGivesCompanyAndCraftBusiness() {
        assertThat(items.get(0).targetGroupsHint()).containsExactlyInAnyOrder(
                TargetGroup.COMPANY, TargetGroup.CRAFT_BUSINESS, TargetGroup.CITIZEN);
    }

    @Test
    void unemployedAndSelfEmploymentGiveCitizen() {
        assertThat(items.get(1).targetGroupsHint()).containsExactly(TargetGroup.CITIZEN);
    }

    @Test
    void measureWithoutCategoryHasNoTargetGroupHint() {
        assertThat(items.get(2).targetGroupsHint()).isEmpty();
    }

    @Test
    void everyMeasureIsContinuousAndNoDeadlineIsInvented() {
        RuleExtractor extractor = new RuleExtractor();

        for (RawItem item : items) {
            ExtractedFields fields = extractor.extract(item);

            assertThat(item.deadlineNoteHint()).isEqualTo("continuous");
            assertThat(fields.deadline()).as(item.title()).isNull();
            assertThat(fields.deadlineNote()).isEqualTo("continuous");
        }
    }
}
