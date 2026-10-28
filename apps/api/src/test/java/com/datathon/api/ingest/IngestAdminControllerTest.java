package com.datathon.api.ingest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.datathon.api.ai.BatchRunner;
import com.datathon.api.common.AdminKeyGuard;
import com.datathon.api.common.GlobalExceptionHandler;
import com.datathon.api.source.SourceCode;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = IngestAdminController.class, properties = "admin.key=test-key")
@Import({AdminKeyGuard.class, GlobalExceptionHandler.class})
class IngestAdminControllerTest {

    private static final String KEY_HEADER = "X-Admin-Key";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IngestService ingestService;

    @MockitoBean
    private BatchRunner batchRunner;

    @Test
    void refusesRequestWithoutKey() throws Exception {
        mockMvc.perform(post("/api/admin/ingest")).andExpect(status().isUnauthorized());

        verifyNoInteractions(ingestService, batchRunner);
    }

    @Test
    void refusesRequestWithWrongKey() throws Exception {
        mockMvc.perform(post("/api/admin/ingest").header(KEY_HEADER, "wrong")).andExpect(status().isUnauthorized());

        verifyNoInteractions(ingestService, batchRunner);
    }

    @Test
    void startsIngestOfAllSourcesInTheBackground() throws Exception {
        givenRun(Optional.empty(), 42L);

        mockMvc.perform(post("/api/admin/ingest").header(KEY_HEADER, "test-key"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.runId").value(42));

        verify(batchRunner).start(eq(IngestAdminController.BATCH_NAME), any());
    }

    @Test
    void startsIngestOfOneSource() throws Exception {
        givenRun(Optional.of(SourceCode.FZOEU), 7L);

        mockMvc.perform(post("/api/admin/ingest").param("source", "FZOEU").header(KEY_HEADER, "test-key"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.runId").value(7))
                .andExpect(jsonPath("$.source").value("FZOEU"));
    }

    @Test
    void unknownSourceIsBadRequest() throws Exception {
        mockMvc.perform(post("/api/admin/ingest").param("source", "NEPOSTOJI").header(KEY_HEADER, "test-key"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(ingestService, batchRunner);
    }

    @Test
    void secondStartWhileRunningIsConflictAndLeavesNoEmptyRun() throws Exception {
        givenRun(Optional.empty(), 5L);
        doThrow(new IllegalStateException("Batch 'ingest' is already running"))
                .when(batchRunner).start(eq(IngestAdminController.BATCH_NAME), any());

        mockMvc.perform(post("/api/admin/ingest").header(KEY_HEADER, "test-key"))
                .andExpect(status().isConflict());

        verify(ingestService).discardRun(5L);
    }

    private void givenRun(Optional<SourceCode> source, Long id) {
        IngestRun run = mock(IngestRun.class);
        when(run.getId()).thenReturn(id);
        when(ingestService.createRun(source)).thenReturn(run);
    }
}
