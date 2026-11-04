package com.datathon.api.fundingcall;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;

import com.datathon.api.common.FacetCount;
import com.datathon.api.common.GlobalExceptionHandler;
import com.datathon.api.common.NotFoundException;
import com.datathon.api.common.PageResponse;
import com.datathon.api.fundingcall.dto.AttachmentResponse;
import com.datathon.api.fundingcall.dto.FundingCallDetail;
import com.datathon.api.fundingcall.dto.FundingCallExport;
import com.datathon.api.fundingcall.dto.FundingCallFacets;
import com.datathon.api.fundingcall.dto.FundingCallFilter;
import com.datathon.api.fundingcall.dto.FundingCallSort;
import com.datathon.api.fundingcall.dto.FundingCallStatusFilter;
import com.datathon.api.fundingcall.dto.FundingCallSummary;
import com.datathon.api.legalbasis.LegalBasis;
import com.datathon.api.source.SourceCode;
import com.datathon.api.source.SourceInfo;
import com.datathon.api.source.SourceRef;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = FundingCallController.class, properties = "app.public-url=https://radar.example")
@Import({GlobalExceptionHandler.class, AtomFeedWriter.class})
class FundingCallControllerTest {

    private static final SourceInfo SOURCE = new SourceInfo(SourceCode.FZOEU, "Fond za zaštitu okoliša", "https://www.fzoeu.hr");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FundingCallService fundingCallService;

    @Test
    void listReturnsOurPageShape() throws Exception {
        var summary = new FundingCallSummary(7L, "Poziv za vozila", "Fond", SourceRef.from(SOURCE), FundingCallType.PUBLIC_CALL,
                FundingCallStatus.OPEN, LocalDate.of(2026, 12, 1), null, null, new BigDecimal("9000.00"),
                List.of(TargetGroup.CITIZEN), List.of(Area.ENERGY, Area.TRANSPORT), List.of("ALL"), false, List.of(), "https://example.org/7", null);
        when(fundingCallService.list(any())).thenReturn(new PageResponse<>(List.of(summary), 1, 0, 20));

        mockMvc.perform(get("/api/funding-calls").param("status", "OPEN").param("targetGroup", "COMPANY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.content[0].id").value(7))
                .andExpect(jsonPath("$.content[0].source.code").value("FZOEU"))
                .andExpect(jsonPath("$.content[0].source.name").value("Fond za zaštitu okoliša"))
                .andExpect(jsonPath("$.content[0].deadline").value("2026-12-01"))
                .andExpect(jsonPath("$.content[0].maxAmount").value(9000.00))
                .andExpect(jsonPath("$.content[0].areas[0]").value("ENERGY"))
                .andExpect(jsonPath("$.content[0].sourceText").doesNotExist());
    }

    @Test
    void listPassesTheParsedFilterToTheService() throws Exception {
        when(fundingCallService.list(any())).thenReturn(new PageResponse<>(List.of(), 0, 2, 50));

        mockMvc.perform(get("/api/funding-calls")
                        .param("q", "stipendija").param("status", "UNKNOWN").param("targetGroup", "STUDENT")
                        .param("area", "EDUCATION").param("county", "Istarska").param("source", "MZOM")
                        .param("type", "SCHOLARSHIP").param("deadlineBefore", "2026-12-31")
                        .param("page", "2").param("size", "50").param("sort", "publishedAt"))
                .andExpect(status().isOk());

        ArgumentCaptor<FundingCallFilter> captor = ArgumentCaptor.forClass(FundingCallFilter.class);
        verify(fundingCallService).list(captor.capture());
        FundingCallFilter filter = captor.getValue();
        org.assertj.core.api.Assertions.assertThat(filter.status()).isEqualTo(FundingCallStatusFilter.UNKNOWN);
        org.assertj.core.api.Assertions.assertThat(filter.sort()).isEqualTo(FundingCallSort.PUBLISHED_AT);
        org.assertj.core.api.Assertions.assertThat(filter.deadlineBefore()).isEqualTo(LocalDate.of(2026, 12, 31));
        org.assertj.core.api.Assertions.assertThat(filter.targetGroup()).isEqualTo(TargetGroup.STUDENT);
        org.assertj.core.api.Assertions.assertThat(filter.source()).isEqualTo(SourceCode.MZOM);
        org.assertj.core.api.Assertions.assertThat(filter.page()).isEqualTo(2);
    }

    @Test
    void listWithoutParametersDefaultsToOpenCallsSortedByDeadline() throws Exception {
        when(fundingCallService.list(any())).thenReturn(new PageResponse<>(List.of(), 0, 0, 20));

        mockMvc.perform(get("/api/funding-calls")).andExpect(status().isOk());

        ArgumentCaptor<FundingCallFilter> captor = ArgumentCaptor.forClass(FundingCallFilter.class);
        verify(fundingCallService).list(captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getValue().status()).isEqualTo(FundingCallStatusFilter.OPEN);
        org.assertj.core.api.Assertions.assertThat(captor.getValue().sort()).isEqualTo(FundingCallSort.DEADLINE);
    }

    @Test
    void detailReturnsAllFieldsAndTheSourceObject() throws Exception {
        var detail = new FundingCallDetail(7L, "Poziv za vozila", "Fond", SOURCE, FundingCallType.PUBLIC_CALL, FundingCallStatus.OPEN,
                LocalDate.of(2026, 3, 2), null, "do iskorištenja sredstava", null, null, new BigDecimal("9000.00"),
                List.of(TargetGroup.CITIZEN), List.of(Area.ENERGY), List.of("ALL"), "Uvjeti", List.of("Zahtjev"),
                List.of(new AttachmentResponse("Poziv", "https://example.org/poziv.pdf")),
                List.of(new LegalBasis("Zakona o Fondu", "107/2003", "https://narodne-novine.nn.hr/eli/sluzbeni/2003/107",
                        null, null, null)), true, List.of("deadline"), false,
                Instant.parse("2026-10-24T08:15:00Z"), "https://example.org/7");
        when(fundingCallService.getById(7L)).thenReturn(detail);

        mockMvc.perform(get("/api/funding-calls/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.source.url").value("https://www.fzoeu.hr"))
                .andExpect(jsonPath("$.conditionsSummary").value("Uvjeti"))
                .andExpect(jsonPath("$.requiredDocuments[0]").value("Zahtjev"))
                .andExpect(jsonPath("$.attachments[0].name").value("Poziv"))
                .andExpect(jsonPath("$.legalBasis[0].gazetteNumber").value("107/2003"))
                .andExpect(jsonPath("$.aiFields[0]").value("deadline"))
                .andExpect(jsonPath("$.fieldsReviewed").value(false))
                .andExpect(jsonPath("$.fetchedAt").value("2026-10-24T08:15:00Z"))
                .andExpect(jsonPath("$.sourceText").doesNotExist());
    }

    @Test
    void facetsReturnValueAndCountPairsPerFilter() throws Exception {
        var facets = new FundingCallFacets(
                List.of(new FacetCount("OPEN", 141), new FacetCount("UNKNOWN", 3)),
                List.of(new FacetCount("STUDENT", 23)),
                List.of(new FacetCount("ENERGY", 8)),
                List.of(new FacetCount("ALL", 60), new FacetCount("Istarska", 4)),
                List.of(new FacetCount("FZOEU", 20)),
                List.of(new FacetCount("SCHOLARSHIP", 12)));
        when(fundingCallService.facets(any())).thenReturn(facets);

        mockMvc.perform(get("/api/funding-calls/facets").param("targetGroup", "STUDENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status[0].value").value("OPEN"))
                .andExpect(jsonPath("$.status[0].count").value(141))
                .andExpect(jsonPath("$.status[1].value").value("UNKNOWN"))
                .andExpect(jsonPath("$.targetGroup[0].value").value("STUDENT"))
                .andExpect(jsonPath("$.area[0].value").value("ENERGY"))
                .andExpect(jsonPath("$.county[0].value").value("ALL"))
                .andExpect(jsonPath("$.source[0].value").value("FZOEU"))
                .andExpect(jsonPath("$.type[0].value").value("SCHOLARSHIP"));

        ArgumentCaptor<FundingCallFilter> captor = ArgumentCaptor.forClass(FundingCallFilter.class);
        verify(fundingCallService).facets(captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getValue().targetGroup()).isEqualTo(TargetGroup.STUDENT);
    }

    @Test
    void facetsWithInvalidFilterReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/funding-calls/facets").param("area", "XYZ"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("area: neispravna vrijednost"));
    }

    @Test
    void detailOfUnknownIdReturnsCroatianNotFound() throws Exception {
        when(fundingCallService.getById(99L)).thenThrow(new NotFoundException("Natječaj ne postoji."));

        mockMvc.perform(get("/api/funding-calls/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404))
                .andExpect(jsonPath("$.message").value("Natječaj ne postoji."))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void nonNumericIdReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/funding-calls/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("id: neispravna vrijednost"));
    }

    @Test
    void unknownEnumValueReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/funding-calls").param("status", "XYZ"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("status: neispravna vrijednost"));
    }

    @Test
    void unknownSortReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/funding-calls").param("sort", "title"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("sort: neispravna vrijednost"));
    }

    @Test
    void invalidDateReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/funding-calls").param("deadlineBefore", "01.12.2026"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("deadlineBefore: neispravna vrijednost"));
    }

    @Test
    void searchTextLongerThanTwoHundredCharactersReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/funding-calls").param("q", "a".repeat(201)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("q: smije imati najviše 200 znakova"));
    }

    @Test
    void exportIsNotMistakenForAnIdAndPointsToTheSchema() throws Exception {
        when(fundingCallService.export(any())).thenReturn(FundingCallExport.of(List.of(), Instant.parse("2026-11-03T10:00:00Z")));

        mockMvc.perform(get("/api/funding-calls/export").param("status", "OPEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conformsTo").value("/api/standard/funding-call.schema.json"))
                .andExpect(jsonPath("$.count").value(0))
                .andExpect(jsonPath("$.records").isArray());
    }

    @Test
    void feedIsAtomWithLinksToOurDetailPages() throws Exception {
        var summary = new FundingCallSummary(7L, "Poziv za vozila & bicikle", "Fond", SourceRef.from(SOURCE), FundingCallType.PUBLIC_CALL,
                FundingCallStatus.OPEN, LocalDate.of(2026, 12, 1), null, null, new BigDecimal("9000.00"),
                List.of(TargetGroup.CITIZEN), List.of(Area.ENERGY), List.of("ALL"), false, List.of(), "https://example.org/7",
                Instant.parse("2026-11-02T08:00:00Z"));
        when(fundingCallService.newest(any())).thenReturn(List.of(summary));

        mockMvc.perform(get("/api/funding-calls/feed.atom").param("targetGroup", "CITIZEN"))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith("application/atom+xml"))
                .andExpect(xpath(
                        "/*[local-name()='feed']/*[local-name()='entry']/*[local-name()='id']")
                        .string("https://radar.example/natjecaji/7"))
                .andExpect(xpath(
                        "/*[local-name()='feed']/*[local-name()='entry']/*[local-name()='title']")
                        .string("Poziv za vozila & bicikle"));
    }
}
