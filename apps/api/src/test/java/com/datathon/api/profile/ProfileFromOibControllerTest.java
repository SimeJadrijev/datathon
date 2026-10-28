package com.datathon.api.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.datathon.api.common.ExternalSourceException;
import com.datathon.api.common.GlobalExceptionHandler;
import com.datathon.api.common.NotFoundException;
import com.datathon.api.fundingcall.Area;
import com.datathon.api.fundingcall.TargetGroup;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(ProfileController.class)
@Import(GlobalExceptionHandler.class)
@ExtendWith(OutputCaptureExtension.class)
class ProfileFromOibControllerTest {

    private static final String OIB = "21173725829";
    private static final String UNKNOWN_OIB = "12345678903";
    private static final String WRONG_CHECK_DIGIT = "21173725828";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProfileMatchService profileMatchService;

    @MockitoBean
    private ProfileFromOibService profileFromOibService;

    @Test
    void validOibReturnsCompanyAndProfile() throws Exception {
        var profile = new ProfileRequest(TargetGroup.COMPANY, List.of(), List.of(Area.DIGITALIZATION), "Grad Zagreb",
                List.of(), List.of("62.01"));
        when(profileFromOibService.fromOib(OIB)).thenReturn(new OibProfileResponse("Microblink d.o.o.",
                new OibProfileResponse.Seat("Zagreb", "Grad Zagreb"), "62.01", profile, "sudreg"));

        postOib(OIB)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyName").value("Microblink d.o.o."))
                .andExpect(jsonPath("$.seat.municipality").value("Zagreb"))
                .andExpect(jsonPath("$.seat.county").value("Grad Zagreb"))
                .andExpect(jsonPath("$.activityCode").value("62.01"))
                .andExpect(jsonPath("$.profile.type").value("COMPANY"))
                .andExpect(jsonPath("$.profile.areas[0]").value("DIGITALIZATION"))
                .andExpect(jsonPath("$.source").value("sudreg"));
    }

    @Test
    void oibWithWrongCheckDigitReturnsBadRequestWithoutCallingSudreg(CapturedOutput output) throws Exception {
        postOib(WRONG_CHECK_DIGIT).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("oib: neispravan OIB"));

        verifyNoInteractions(profileFromOibService);
        assertThat(output.getAll()).doesNotContain(WRONG_CHECK_DIGIT);
    }

    @Test
    void tooShortAndNonNumericOibsReturnBadRequest() throws Exception {
        postOib("2117372582").andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("oib: neispravan OIB"));
        postOib("2117372582x").andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("oib: neispravan OIB"));
        verifyNoInteractions(profileFromOibService);
    }

    @Test
    void missingOibReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/profiles/from-oib").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("oib: obavezno polje"));
    }

    @Test
    void unknownOibReturnsNotFound(CapturedOutput output) throws Exception {
        when(profileFromOibService.fromOib(any())).thenThrow(new NotFoundException("Tvrtka s ovim OIB-om nije pronađena."));

        postOib(UNKNOWN_OIB).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Tvrtka s ovim OIB-om nije pronađena."));

        assertThat(output.getAll()).doesNotContain(UNKNOWN_OIB);
    }

    @Test
    void sudregFailureReturnsBadGateway(CapturedOutput output) throws Exception {
        when(profileFromOibService.fromOib(any())).thenThrow(new ExternalSourceException("Sudreg", "Sudreg is unreachable"));

        postOib(OIB).andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.message").value("Vanjski izvor (Sudreg) trenutno nije dostupan. Pokušajte kasnije."));

        assertThat(output.getAll()).doesNotContain(OIB);
    }

    private ResultActions postOib(String oib) throws Exception {
        return mockMvc.perform(post("/api/profiles/from-oib").contentType(MediaType.APPLICATION_JSON)
                .content("{\"oib\": \"" + oib + "\"}"));
    }
}
