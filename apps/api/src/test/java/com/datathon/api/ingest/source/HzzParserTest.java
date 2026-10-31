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
    void activeMeasureIsContinuousAndNoDeadlineIsInvented() {
        ExtractedFields fields = new RuleExtractor().extract(items.getFirst());

        assertThat(fields.deadline()).isNull();
        assertThat(fields.deadlineNote()).isEqualTo("kontinuirano, bez roka");
    }

    @Test
    void closedMeasureDoesNotClaimToBeContinuous() {
        for (RawItem closed : items.subList(1, items.size())) {
            ExtractedFields fields = new RuleExtractor().extract(closed);

            assertThat(fields.deadline()).as(closed.title()).isNull();
            assertThat(fields.deadlineNote()).isEqualTo("mjera više nije aktivna");
        }
    }

    @Test
    void noteIsInCroatianLikeTheOtherSources() {
        assertThat(items).extracting(RawItem::deadlineNoteHint).doesNotContain("continuous");
    }

    @Test
    void measureWithMissingOrBrokenDateIsKeptWithoutPublicationDate() {
        var measures = SourceFixtures.json("hzz/measures.json");
        var json = SourceFixtures.text("hzz/measures.json").replace(measures.get(0).path("date").asString(), "");
        var broken = SourceFixtures.text("hzz/measures.json").replace(measures.get(0).path("date").asString(), "2026-13-45T10:00:00");

        for (String body : List.of(json, broken)) {
            var parsed = HzzParser.parseMeasures(tools.jackson.databind.json.JsonMapper.builder().build().readTree(body),
                    HzzParser.termNames(SourceFixtures.json("hzz/years.json")),
                    HzzParser.termNames(SourceFixtures.json("hzz/categories.json")));

            assertThat(parsed).hasSize(3);
            assertThat(parsed.getFirst().publishedAt()).isNull();
        }
    }
}
