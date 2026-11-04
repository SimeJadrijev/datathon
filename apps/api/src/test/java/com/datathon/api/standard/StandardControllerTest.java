package com.datathon.api.standard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.datathon.api.fundingcall.FundingCall;
import com.datathon.api.fundingcall.FundingCallStatus;
import com.datathon.api.fundingcall.dto.FundingCallExport;
import com.datathon.api.fundingcall.dto.FundingCallRecord;
import com.datathon.api.source.SourceCode;
import com.datathon.api.source.SourceInfo;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(StandardController.class)
class StandardControllerTest {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired
    private MockMvc mockMvc;

    @Test
    void servesTheSchemaUnderTheAddressTheExportPointsTo() throws Exception {
        mockMvc.perform(get(FundingCallExport.SCHEMA_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Otvoreni podaci o natječajima"))
                .andExpect(jsonPath("$.$defs.fundingCall.required").isArray());
    }

    @Test
    void exportedRecordHasEveryFieldTheSchemaRequires() throws Exception {
        JsonNode schema = JSON.readTree(new ClassPathResource(StandardController.SCHEMA_FILE).getInputStream());
        FundingCall fundingCall = new FundingCall(SourceCode.FZOEU, "275", "Poziv za vozila");
        fundingCall.setStatus(FundingCallStatus.OPEN);
        SourceInfo source = new SourceInfo(SourceCode.FZOEU, "Fond", "https://www.fzoeu.hr");
        JsonNode export = JSON.valueToTree(FundingCallExport.of(
                List.of(FundingCallRecord.from(fundingCall, source, List.of())), Instant.now()));

        assertThat(fieldNames(export)).containsAll(required(schema));
        assertThat(fieldNames(export.path("records").get(0))).containsAll(required(schema.path("$defs").path("fundingCall")));
    }

    private static List<String> required(JsonNode schema) {
        List<String> names = new ArrayList<>();
        schema.path("required").forEach(name -> names.add(name.asString()));
        return names;
    }

    private static List<String> fieldNames(JsonNode node) {
        return new ArrayList<>(node.propertyNames());
    }
}
