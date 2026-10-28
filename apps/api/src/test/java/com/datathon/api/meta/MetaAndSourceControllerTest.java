package com.datathon.api.meta;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.datathon.api.source.SourceCode;
import com.datathon.api.source.SourceController;
import com.datathon.api.source.SourceMethod;
import com.datathon.api.source.SourceService;
import com.datathon.api.source.SourceSummary;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({MetaController.class, SourceController.class})
class MetaAndSourceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MetaService metaService;

    @MockitoBean
    private SourceService sourceService;

    @Test
    void metaReturnsSnapshotIngestAndCounts() throws Exception {
        when(metaService.get()).thenReturn(new MetaResponse(
                LocalDate.of(2026, 10, 26), null, new MetaResponse.Counts(141, 3860)));

        mockMvc.perform(get("/api/meta"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.snapshotDate").value("2026-10-26"))
                .andExpect(jsonPath("$.lastIngestAt").value((Object) null))
                .andExpect(jsonPath("$.counts.openFundingCalls").value(141))
                .andExpect(jsonPath("$.counts.regulations").value(3860));
    }

    @Test
    void sourcesReturnCatalogEntriesWithCounts() throws Exception {
        when(sourceService.list()).thenReturn(List.of(
                new SourceSummary(SourceCode.HZZ, "HZZ", "Mjere", "https://mjere.hzz.hr", SourceMethod.API,
                        "nije provjereno", 97L, Instant.parse("2026-10-26T00:00:00Z")),
                new SourceSummary(SourceCode.SUDREG, "Sudski registar", "Profil iz OIB-a", "https://sudreg-data.gov.hr",
                        SourceMethod.API, "nije provjereno", null, null)));

        mockMvc.perform(get("/api/sources"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("HZZ"))
                .andExpect(jsonPath("$[0].method").value("API"))
                .andExpect(jsonPath("$[0].recordCount").value(97))
                .andExpect(jsonPath("$[0].lastSuccessfulFetch").value("2026-10-26T00:00:00Z"))
                .andExpect(jsonPath("$[1].code").value("SUDREG"))
                .andExpect(jsonPath("$[1].recordCount").value((Object) null));
    }
}
