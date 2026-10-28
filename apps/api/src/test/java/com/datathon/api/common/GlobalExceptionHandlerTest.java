package com.datathon.api.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ErrorTestController.class)
@Import({ErrorTestController.class, GlobalExceptionHandler.class})
@ExtendWith(OutputCaptureExtension.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void notFoundReturnsCroatianMessageFromException() throws Exception {
        mockMvc.perform(get("/test-errors/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404))
                .andExpect(jsonPath("$.message").value("Natječaj ne postoji."))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void unknownRouteReturnsNotFoundInOurShape() throws Exception {
        mockMvc.perform(get("/api/does-not-exist"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404))
                .andExpect(jsonPath("$.message").value("Tražena adresa ne postoji."))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void invalidQueryParameterReturnsFirstFieldAndReason() throws Exception {
        mockMvc.perform(get("/test-errors/query").param("q", "too long"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.message").value("q: smije imati najviše 5 znakova"))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    void invalidBodyReturnsFirstFieldAndReason() throws Exception {
        mockMvc.perform(post("/test-errors/body").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\" \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("name: ne smije biti prazno"))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    void unreadableBodyReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/test-errors/body").contentType(MediaType.APPLICATION_JSON).content("{not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.message").value("Neispravno tijelo zahtjeva."));
    }

    @Test
    void wrongTypeNamesTheParameterAndNeverEchoesTheValue(CapturedOutput output) throws Exception {
        mockMvc.perform(get("/test-errors/query").param("date", "31-12-secret"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("date: neispravna vrijednost"))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("secret"))));

        assertThat(output.getAll()).doesNotContain("secret");
    }

    @Test
    void missingRequiredParameterReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/test-errors/required"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("needed: obavezan parametar"));
    }

    @Test
    void invalidRequestExceptionReturnsItsMessage() throws Exception {
        mockMvc.perform(get("/test-errors/invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("publishedFrom: ne smije biti nakon publishedTo"));
    }

    @Test
    void externalSourceFailureReturnsBadGatewayWithoutInternalDetail() throws Exception {
        mockMvc.perform(get("/test-errors/external"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.statusCode").value(502))
                .andExpect(jsonPath("$.message")
                        .value("Vanjski izvor (Sudreg) trenutno nije dostupan. Pokušajte kasnije."))
                .andExpect(jsonPath("$.error").value("Bad Gateway"))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("secret-detail"))));
    }

    @Test
    void unexpectedErrorReturnsGenericMessageAndKeepsTheStackTraceInTheLog(CapturedOutput output) throws Exception {
        mockMvc.perform(get("/test-errors/boom"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.statusCode").value(500))
                .andExpect(jsonPath("$.message").value("Došlo je do pogreške na poslužitelju."))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("secret-detail"))));

        assertThat(output.getAll()).contains("IllegalStateException");
    }

    @Test
    void pageSizeAboveTheLimitIsCutToOneHundred() throws Exception {
        mockMvc.perform(get("/test-errors/page").param("size", "1000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(100))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.totalElements").value(250))
                .andExpect(jsonPath("$.content[0]").value("A"));
    }

    @Test
    void pageDefaultsToTwentyItemsFromThePageZero() throws Exception {
        mockMvc.perform(get("/test-errors/page"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.page").value(0));
    }

    @Test
    void negativePageAndZeroSizeAreRejected() throws Exception {
        mockMvc.perform(get("/test-errors/page").param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("page: ne smije biti negativan"));
        mockMvc.perform(get("/test-errors/page").param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("size: mora biti najmanje 1"));
    }
}
