package com.datathon.api.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.datathon.api.common.GlobalExceptionHandler;
import com.datathon.api.fundingcall.Area;
import com.datathon.api.fundingcall.FundingCallStatus;
import com.datathon.api.fundingcall.FundingCallType;
import com.datathon.api.fundingcall.TargetGroup;
import com.datathon.api.fundingcall.dto.FundingCallSummary;
import com.datathon.api.source.SourceCode;
import com.datathon.api.source.SourceRef;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(ProfileController.class)
@Import(GlobalExceptionHandler.class)
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProfileMatchService profileMatchService;

    @MockitoBean
    private ProfileFromOibService profileFromOibService;

    @Test
    void matchReturnsResultsWithReasons() throws Exception {
        var summary = new FundingCallSummary(7L, "Vaučeri za digitalizaciju", "Ministarstvo",
                new SourceRef(SourceCode.EU_FUNDS_NPOO, "EU fondovi (NPOO)"), FundingCallType.PUBLIC_CALL,
                FundingCallStatus.OPEN, LocalDate.of(2026, 12, 1), null, null, null, List.of(TargetGroup.COMPANY),
                List.of(Area.DIGITALIZATION), List.of("ALL"), false, "https://example.org/7", null);
        when(profileMatchService.match(any()))
                .thenReturn(List.of(new MatchResult(summary, 9.0, List.of("ciljna skupina: tvrtke", "otvoren"), false)));

        postProfile("""
                {"type": "COMPANY", "additionalTypes": ["CRAFT_BUSINESS"], "areas": ["DIGITALIZATION"],
                 "county": "Zagreb", "keywords": ["digital"], "activityCodes": ["62.01"]}
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fundingCall.id").value(7))
                .andExpect(jsonPath("$[0].fundingCall.source.code").value("EU_FUNDS_NPOO"))
                .andExpect(jsonPath("$[0].score").value(9.0))
                .andExpect(jsonPath("$[0].reasons[0]").value("ciljna skupina: tvrtke"))
                .andExpect(jsonPath("$[0].statusUnknown").value(false));

        ArgumentCaptor<ProfileRequest> captor = ArgumentCaptor.forClass(ProfileRequest.class);
        verify(profileMatchService).match(captor.capture());
        assertThat(captor.getValue().type()).isEqualTo(TargetGroup.COMPANY);
        assertThat(captor.getValue().activityCodes()).containsExactly("62.01");
    }

    @Test
    void profileWithOnlyAKeywordIsValid() throws Exception {
        when(profileMatchService.match(any())).thenReturn(List.of());

        postProfile("{\"type\": \"STUDENT\", \"keywords\": [\"stipendij\"]}").andExpect(status().isOk());
    }

    @Test
    void emptyBodyReturnsBadRequest() throws Exception {
        postProfile("").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Neispravno tijelo zahtjeva."));
        verifyNoInteractions(profileMatchService);
    }

    @Test
    void missingTypeReturnsBadRequest() throws Exception {
        postProfile("{\"areas\": [\"ENERGY\"]}").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("type: obavezno polje"));
    }

    @Test
    void unknownTypeReturnsBadRequest() throws Exception {
        postProfile("{\"type\": \"ROBOT\", \"areas\": [\"ENERGY\"]}").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Neispravno tijelo zahtjeva."));
    }

    @Test
    void profileWithoutAreasKeywordsOrActivityCodesReturnsBadRequest() throws Exception {
        postProfile("{\"type\": \"COMPANY\", \"county\": \"Zagreb\"}").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Odaberite barem jedno područje, ključnu riječ ili djelatnost."));
    }

    @Test
    void tooManyKeywordsReturnBadRequest() throws Exception {
        String keywords = String.join(",", Collections.nCopies(11, "\"abc\""));

        postProfile("{\"type\": \"COMPANY\", \"keywords\": [" + keywords + "]}").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("keywords: smije imati najviše 10 stavki"));
    }

    @Test
    void overlongKeywordReturnsBadRequest() throws Exception {
        postProfile("{\"type\": \"COMPANY\", \"keywords\": [\"" + "a".repeat(51) + "\"]}").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("keywords[0]: mora imati od 3 do 50 znakova"));
    }

    @Test
    void malformedActivityCodeReturnsBadRequest() throws Exception {
        postProfile("{\"type\": \"COMPANY\", \"activityCodes\": [\"6201\"]}").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("activityCodes[0]: neispravan oblik šifre djelatnosti"));
    }

    private ResultActions postProfile(String body) throws Exception {
        return mockMvc.perform(post("/api/profiles/match").contentType(MediaType.APPLICATION_JSON).content(body));
    }
}
