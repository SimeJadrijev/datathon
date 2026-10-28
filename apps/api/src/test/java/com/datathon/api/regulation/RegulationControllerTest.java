package com.datathon.api.regulation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.datathon.api.common.FacetCount;
import com.datathon.api.common.GlobalExceptionHandler;
import com.datathon.api.common.InvalidRequestException;
import com.datathon.api.common.NotFoundException;
import com.datathon.api.common.PageResponse;
import com.datathon.api.fundingcall.Area;
import com.datathon.api.regulation.dto.RegulationDetail;
import com.datathon.api.regulation.dto.RegulationFacets;
import com.datathon.api.regulation.dto.RegulationFilter;
import com.datathon.api.regulation.dto.RegulationSummary;
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

@WebMvcTest(RegulationController.class)
@Import(GlobalExceptionHandler.class)
class RegulationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegulationService regulationService;

    @Test
    void listReturnsOurPageShape() throws Exception {
        var summary = new RegulationSummary(5L, "Zakon o energiji", RegulationType.LAW, "12/2026",
                LocalDate.of(2026, 2, 1), List.of(Area.ENERGY), true);
        when(regulationService.list(any())).thenReturn(new PageResponse<>(List.of(summary), 1, 0, 20));

        mockMvc.perform(get("/api/regulations").param("type", "LAW").param("publishedFrom", "2026-01-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(5))
                .andExpect(jsonPath("$.content[0].type").value("LAW"))
                .andExpect(jsonPath("$.content[0].gazetteNumber").value("12/2026"))
                .andExpect(jsonPath("$.content[0].publishedAt").value("2026-02-01"))
                .andExpect(jsonPath("$.content[0].areas[0]").value("ENERGY"))
                .andExpect(jsonPath("$.content[0].hasSummary").value(true));

        ArgumentCaptor<RegulationFilter> captor = ArgumentCaptor.forClass(RegulationFilter.class);
        verify(regulationService).list(captor.capture());
        assertThat(captor.getValue().type()).isEqualTo(RegulationType.LAW);
        assertThat(captor.getValue().publishedFrom()).isEqualTo(LocalDate.of(2026, 1, 1));
    }

    @Test
    void detailReturnsAllFieldsAndNullSummary() throws Exception {
        var detail = new RegulationDetail(5L, "Zakon o energiji", RegulationType.LAW, "12/2026", 150,
                LocalDate.of(2026, 2, 1), LocalDate.of(2026, 1, 20), List.of(Area.ENERGY), null,
                "https://narodne-novine.nn.hr/clanci/sluzbeni/2026_02_12_150.html",
                "https://narodne-novine.nn.hr/eli/sluzbeni/2026/12/150", "https://narodne-novine.nn.hr/pdf/150.pdf",
                true, Instant.parse("2026-10-24T08:15:00Z"));
        when(regulationService.getById(5L)).thenReturn(detail);

        mockMvc.perform(get("/api/regulations/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").value((Object) null))
                .andExpect(jsonPath("$.documentNumber").value(150))
                .andExpect(jsonPath("$.enactedAt").value("2026-01-20"))
                .andExpect(jsonPath("$.eliUrl").value("https://narodne-novine.nn.hr/eli/sluzbeni/2026/12/150"))
                .andExpect(jsonPath("$.pdfUrl").value("https://narodne-novine.nn.hr/pdf/150.pdf"))
                .andExpect(jsonPath("$.eliMetadata").value(true))
                .andExpect(jsonPath("$.fetchedAt").value("2026-10-24T08:15:00Z"));
    }

    @Test
    void detailOfUnknownIdReturnsCroatianNotFound() throws Exception {
        when(regulationService.getById(99L)).thenThrow(new NotFoundException("Propis ne postoji."));

        mockMvc.perform(get("/api/regulations/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404))
                .andExpect(jsonPath("$.message").value("Propis ne postoji."));
    }

    @Test
    void invertedPublishedRangeReturnsBadRequest() throws Exception {
        when(regulationService.list(any()))
                .thenThrow(new InvalidRequestException("publishedFrom: ne smije biti nakon publishedTo"));

        mockMvc.perform(get("/api/regulations").param("publishedFrom", "2026-05-01").param("publishedTo", "2026-04-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("publishedFrom: ne smije biti nakon publishedTo"));
    }

    @Test
    void unknownTypeReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/regulations").param("type", "XYZ"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("type: neispravna vrijednost"));
    }

    @Test
    void facetsReturnValueAndCountPairs() throws Exception {
        when(regulationService.facets(any())).thenReturn(new RegulationFacets(
                List.of(new FacetCount("LAW", 40), new FacetCount("DECREE", 12)),
                List.of(new FacetCount("ENERGY", 7))));

        mockMvc.perform(get("/api/regulations/facets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type[0].value").value("LAW"))
                .andExpect(jsonPath("$.type[0].count").value(40))
                .andExpect(jsonPath("$.area[0].value").value("ENERGY"));
    }
}
