package com.datathon.api.ingest.source;

import static com.datathon.api.ingest.source.SourceFixtures.json;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.datathon.api.fundingcall.FundingCall;
import com.datathon.api.fundingcall.FundingCallStatus;
import com.datathon.api.fundingcall.FundingCallType;
import com.datathon.api.fundingcall.TargetGroup;
import com.datathon.api.ingest.RawItem;
import com.datathon.api.ingest.http.PoliteHttpClient;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

class EuFundsCohesionParserTest {

    private List<RawItem> items;

    @BeforeEach
    void parseFixture() {
        items = EuFundsCohesionParser.parsePage(json("eu-funds-cohesion/browse.json"));
    }

    @Test
    void keepsOnlyOpenAndRestrictedCallsAndDropsDirectAwards() {
        assertThat(items).extracting(RawItem::title).noneMatch(title -> title.startsWith("Podrška za projekte EDIH"));
        assertThat(items).hasSize(3);
    }

    @Test
    void mapsStructuredFields() {
        RawItem hydrogen = items.getFirst();

        assertThat(hydrogen.externalId()).isEqualTo("7b0a9385-da8d-431d-9f9c-b0cd210bd616");
        assertThat(hydrogen.url()).isEqualTo("https://eufondovi.gov.hr/poziv/?id=7b0a9385-da8d-431d-9f9c-b0cd210bd616");
        assertThat(hydrogen.title()).isEqualTo("Ulaganje u razvoj elektrolizatora za vodik");
        assertThat(hydrogen.provider()).isEqualTo("Ministarstvo gospodarstva");
        assertThat(hydrogen.publishedAt()).isEqualTo(LocalDate.of(2026, 7, 21));
        assertThat(hydrogen.deadlineHint()).isEqualTo(LocalDate.of(2027, 7, 21));
        assertThat(hydrogen.statusHint()).isEqualTo(FundingCallStatus.OPEN);
        assertThat(hydrogen.typeHint()).isEqualTo(FundingCallType.GRANT);
        assertThat(hydrogen.countiesHint()).containsExactly(FundingCall.ALL_COUNTIES);
        assertThat(hydrogen.categories()).containsExactly("Velika poduzeća", "Obnovljivi izvori energije");
        assertThat(hydrogen.totalAmountHint()).isEqualByComparingTo("13500000");
        assertThat(hydrogen.minAmountHint()).isEqualByComparingTo("500000");
        assertThat(hydrogen.maxAmountHint()).isEqualByComparingTo("10000000");
        assertThat(hydrogen.text()).contains("Oznaka poziva: ", "Prihvatljivi prijavitelji: Mikro poduzetnici");
    }

    @Test
    void applicantsGiveTargetGroups() {
        assertThat(items.getFirst().targetGroupsHint())
                .containsExactlyInAnyOrder(TargetGroup.COMPANY, TargetGroup.CRAFT_BUSINESS);
        assertThat(items.get(1).targetGroupsHint()).isEmpty();
    }

    @Test
    void zeroMinimumIsUnknownAndFinishedCallIsClosed() {
        RawItem restricted = items.get(1);
        RawItem finished = items.get(2);

        assertThat(restricted.minAmountHint()).isNull();
        assertThat(restricted.maxAmountHint()).isEqualByComparingTo(BigDecimal.valueOf(700000));
        assertThat(finished.statusHint()).isEqualTo(FundingCallStatus.CLOSED);
        assertThat(finished.targetGroupsHint()).isEmpty();
    }

    @Test
    void fetcherPagesUntilTotalCount() {
        PoliteHttpClient http = mock(PoliteHttpClient.class);
        when(http.getJson(EuFundsCohesionParser.pageUrl(0, 100))).thenReturn(page(150, 100));
        when(http.getJson(EuFundsCohesionParser.pageUrl(100, 100))).thenReturn(page(150, 50));

        assertThat(new EuFundsCohesionFetcher(http).fetchList()).hasSize(150);
    }

    private static ObjectNode page(int total, int size) {
        JsonMapper mapper = JsonMapper.builder().build();
        ObjectNode page = mapper.createObjectNode().put("TotalCount", total);
        ArrayNode records = page.putArray("Records");
        for (int i = 0; i < size; i++) {
            records.addObject().put("ID", "id-" + total + "-" + i + "-" + size)
                    .put("Naziv", "Poziv " + i)
                    .put("VrstaPostupkaDodjeleNaziv", "Otvoreni poziv")
                    .put("Status", "Otvoren");
        }
        return page;
    }
}
