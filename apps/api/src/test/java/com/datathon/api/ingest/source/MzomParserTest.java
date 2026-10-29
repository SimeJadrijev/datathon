package com.datathon.api.ingest.source;

import static com.datathon.api.ingest.source.SourceFixtures.text;
import static org.assertj.core.api.Assertions.assertThat;

import com.datathon.api.fundingcall.FundingCall;
import com.datathon.api.fundingcall.FundingCallType;
import com.datathon.api.fundingcall.TargetGroup;
import com.datathon.api.ingest.RawItem;
import com.datathon.api.ingest.extract.FundingFilter;
import com.datathon.api.ingest.extract.RuleExtractor;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MzomParserTest {

    private final FundingFilter fundingFilter = new FundingFilter();

    private List<RawItem> items;

    @BeforeEach
    void parseFixture() {
        items = MzomParser.parseListPage(text("mzom/list.html"));
    }

    @Test
    void parsesListPage() {
        assertThat(items).hasSize(10);
        RawItem first = items.getFirst();
        assertThat(first.externalId()).isEqualTo("8127");
        assertThat(first.url()).startsWith("https://mzom.gov.hr/");
        assertThat(first.title()).startsWith("Poziv na podnošenje projektnih prijedloga");
        assertThat(first.publishedAt()).isEqualTo(LocalDate.of(2026, 10, 27));
        assertThat(first.deadlineHint()).isEqualTo(LocalDate.of(2026, 11, 30));
        assertThat(first.categories()).containsExactly("Javni pozivi", "Znanost i tehnologija");
        assertThat(first.countiesHint()).containsExactly(FundingCall.ALL_COUNTIES);
    }

    @Test
    void deadlineIsFilledForEveryItemThatShowsOne() {
        assertThat(items).filteredOn(item -> byId(item, "8116", "8114", "8096", "8127"))
                .extracting(RawItem::deadlineHint).doesNotContainNull();
        assertThat(byId("8114").deadlineHint()).isEqualTo(LocalDate.of(2026, 11, 11));
        assertThat(byId("8116").deadlineHint()).isEqualTo(LocalDate.of(2027, 2, 1));
    }

    @Test
    void scholarshipsGetTypeAndStudentGroup() {
        RawItem scholarship = byId("8114");

        assertThat(scholarship.typeHint()).isEqualTo(FundingCallType.SCHOLARSHIP);
        assertThat(scholarship.targetGroupsHint()).containsExactly(TargetGroup.STUDENT);
        assertThat(byId("8127").typeHint()).isNull();
    }

    @Test
    void savedSampleWithJobAdsProducesNoFundingCallFromThem() {
        List<String> kept = items.stream()
                .filter(item -> !MzomParser.isJobRelated(item))
                .filter(item -> fundingFilter.isFunding(item.title()))
                .map(RawItem::externalId)
                .toList();

        assertThat(kept).containsExactlyInAnyOrder("8127", "8116", "8114", "8096", "8080");
        assertThat(kept).doesNotContain("8124", "8122", "8109", "8097", "8092");
    }

    @Test
    void jobAdsAreDroppedByTitleOrByTheirPersonnelTheme() {
        assertThat(fundingFilter.isFunding(byId("8109").title())).isFalse();
        assertThat(fundingFilter.isFunding(byId("8092").title())).isTrue();
        assertThat(MzomParser.isJobRelated(byId("8092"))).isTrue();
        assertThat(MzomParser.isJobRelated(byId("8114"))).isFalse();
    }

    @Test
    void detailGivesFullTextAndDocuments() {
        RawItem detailed = MzomParser.applyDetail(byId("8114"), text("mzom/detail.html"));

        assertThat(detailed.text()).contains("JAVNI POZIV").contains("STEM");
        assertThat(detailed.attachments()).isNotEmpty().allSatisfy(attachment ->
                assertThat(attachment.url()).startsWith("https://mzom.gov.hr/UserDocsImages"));
        assertThat(detailed.deadlineHint()).isEqualTo(LocalDate.of(2026, 11, 11));
    }

    @Test
    void detailFillsDeadlineWhenTheListHadNone() {
        RawItem withoutDeadline = byId("8114").toBuilder().deadlineHint(null).build();

        RawItem detailed = MzomParser.applyDetail(withoutDeadline, text("mzom/detail.html"));

        assertThat(detailed.deadlineHint()).isEqualTo(LocalDate.of(2026, 11, 11));
    }

    @Test
    void ruleExtractorKeepsTheDeadlineFromTheListAndTheScholarshipType() {
        RawItem detailed = MzomParser.applyDetail(byId("8114"), text("mzom/detail.html"));

        var fields = new RuleExtractor().extract(detailed);

        assertThat(fields.deadline()).isEqualTo(LocalDate.of(2026, 11, 11));
        assertThat(fields.type()).isEqualTo(FundingCallType.SCHOLARSHIP);
        assertThat(fields.targetGroups()).contains(TargetGroup.STUDENT);
    }

    private RawItem byId(String id) {
        return items.stream().filter(item -> item.externalId().equals(id)).findFirst().orElseThrow();
    }

    private static boolean byId(RawItem item, String... ids) {
        return List.of(ids).contains(item.externalId());
    }
}
