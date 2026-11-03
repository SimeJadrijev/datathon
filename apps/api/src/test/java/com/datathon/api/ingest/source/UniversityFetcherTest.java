package com.datathon.api.ingest.source;

import static com.datathon.api.ingest.source.SourceFixtures.text;
import static org.assertj.core.api.Assertions.assertThat;

import com.datathon.api.fundingcall.TargetGroup;
import com.datathon.api.ingest.RawItem;
import com.datathon.api.ingest.extract.CallTitleFilter;
import com.datathon.api.ingest.extract.FundingFilter;
import com.datathon.api.ingest.extract.RuleExtractor;
import com.datathon.api.ingest.source.LocalHtmlListFetcher.HtmlListSite;
import java.util.List;
import org.junit.jupiter.api.Test;

class UniversityFetcherTest {

    private static final CallTitleFilter CALLS = new CallTitleFilter(new FundingFilter());

    @Test
    void zagrebErasmusListTakesTitlesFromTheTitleAttribute() {
        List<RawItem> items = LocalHtmlListFetcher.parseList(text("universities/unizg-erasmus.html"), site("unizg.hr"));

        assertThat(items).hasSizeGreaterThanOrEqualTo(18).allMatch(item -> !item.title().isBlank() && item.url().startsWith("https://www.unizg.hr/"));
        assertThat(items).extracting(RawItem::title).filteredOn(CALLS::isStudentCall).hasSizeGreaterThan(15);
        assertThat(items.getFirst().countiesHint()).containsExactly("Grad Zagreb");
    }

    @Test
    void studentCouncilNoticesOnOnePageGetSeparateIdsAndOnlyTheCallStays() {
        List<RawItem> items = LocalHtmlListFetcher.parseList(text("universities/unipu-student-council.html"), site("unipu.hr"));

        assertThat(items).extracting(RawItem::externalId).doesNotHaveDuplicates();
        assertThat(items).extracting(RawItem::title).filteredOn(CALLS::isStudentCall)
                .anyMatch(title -> title.startsWith("Odluku o raspisivanju natječaja za financiranje studentskih programa"))
                .noneMatch(title -> title.startsWith("P R I J E D L O G") || title.startsWith("Rezultati"));
    }

    @Test
    void itemsWithoutTheirOwnPageAreKeptOncePerTitle() {
        List<RawItem> items = LocalHtmlListFetcher.parseList(text("universities/unizd-erasmus.html"), site("international.unizd.hr"));

        assertThat(items).hasSizeGreaterThanOrEqualTo(3);
        assertThat(items).extracting(RawItem::externalId).doesNotHaveDuplicates();
        assertThat(items).extracting(RawItem::title).filteredOn(CALLS::isStudentCall)
                .noneMatch(title -> title.startsWith("Podsjećamo"));
    }

    @Test
    void studentFilterKeepsMobilityHousingAndAssociationFundingButNotJobs() {
        assertThat(CALLS.isStudentCall("Erasmus+ KA131 – 2. krug natječaja za mobilnost studenata za stručnu praksu")).isTrue();
        assertThat(CALLS.isStudentCall("Natječaj za subvencionirano stanovanje studenata u akademskoj godini 2026./2027.")).isTrue();
        assertThat(CALLS.isStudentCall("Natječaj za financiranje studentskih organizacija 2026")).isTrue();
        assertThat(CALLS.isStudentCall("Natječaj za izbor u znanstveno-nastavno zvanje docenta")).isFalse();
        assertThat(CALLS.isStudentCall("Natječaj za voditelje ureda Studentskog zbora Sveučilišta u Rijeci")).isFalse();
        assertThat(CALLS.isStudentCall("3. Odluka o odabiru osoblja za aktivnosti mobilnosti – Erasmus+")).isFalse();
        assertThat(CALLS.isStudentCall("SPUŠTANJE BODNE GRANICE ZA SUBVENCIONIRANI SMJEŠTAJ")).isFalse();
    }

    @Test
    void studentOrganisationCallIsForAssociationsToo() {
        RawItem call = RawItem.builder().externalId("1").title("Natječaj za financiranje studentskih organizacija 2026")
                .text("Pravo prijave imaju studentske organizacije upisane u Upisnik.").build();

        assertThat(new RuleExtractor().extract(call).targetGroups()).contains(TargetGroup.ASSOCIATION, TargetGroup.STUDENT);
    }

    private static HtmlListSite site(String host) {
        return UniversityFetcher.HTML_LISTS.stream().filter(list -> list.site().url().contains("//www." + host)
                        || list.site().url().contains("//" + host)).findFirst().orElseThrow();
    }
}
