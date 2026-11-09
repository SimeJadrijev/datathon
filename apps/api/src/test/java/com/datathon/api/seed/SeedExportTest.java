package com.datathon.api.seed;

import static org.assertj.core.api.Assertions.assertThat;

import com.datathon.api.fundingcall.Attachment;
import com.datathon.api.fundingcall.FundingCall;
import com.datathon.api.fundingcall.TargetGroup;
import com.datathon.api.regulation.Regulation;
import com.datathon.api.source.SourceCode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class SeedExportTest {

    private static final String QUESTIONS = "[{\"id\":\"q1\",\"text\":\"Jeste li obrt?\",\"type\":\"YES_NO\",\"options\":[]}]";

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Test
    void fundingCallKeepsAiResultsThroughJson() {
        FundingCall original = new FundingCall(SourceCode.FZOEU, "275", "Električna vozila");
        original.setMaxAmount(new BigDecimal("9000"));
        original.getTargetGroups().add(TargetGroup.CITIZEN);
        original.setConditionsSummary("Mogu se prijaviti građani.");
        original.getRequiredDocuments().add("Prijavni obrazac");
        original.getAttachments().add(new Attachment("Poziv", "https://example.hr/poziv.pdf"));
        original.setExtractedByLlm(true);
        original.getAiFields().add("maxAmount");
        original.setEligibilityQuestions(QUESTIONS);
        original.setDeadlineNote("do iskorištenja sredstava");
        original.setFieldsReviewed(true);

        FundingCallSnapshot snapshot = new FundingCallSnapshot(LocalDate.of(2026, 11, 3),
                List.of(SeedFundingCall.from(original)));
        FundingCall restored = jsonMapper.readValue(jsonMapper.writeValueAsString(snapshot), FundingCallSnapshot.class)
                .fundingCalls().getFirst().toNewEntity();

        assertThat(restored.getEligibilityQuestions()).isEqualTo(QUESTIONS);
        assertThat(restored.isExtractedByLlm()).isTrue();
        assertThat(restored.getAiFields()).containsExactly("maxAmount");
        assertThat(restored.getConditionsSummary()).isEqualTo("Mogu se prijaviti građani.");
        assertThat(restored.getDeadlineNote()).isEqualTo("do iskorištenja sredstava");
        assertThat(restored.isFieldsReviewed()).isTrue();
        assertThat(restored.getMaxAmount()).isEqualByComparingTo("9000");
        assertThat(restored.getAttachments()).singleElement()
                .satisfies(attachment -> assertThat(attachment.getUrl()).endsWith("poziv.pdf"));
    }

    @Test
    void regulationKeepsSummaryThroughJson() {
        Regulation original = new Regulation("2026-121-1", "Zakon o testiranju");
        original.setSummary("Zakon uređuje testiranje.");

        RegulationSnapshot snapshot = new RegulationSnapshot(LocalDate.of(2026, 11, 3),
                List.of(SeedRegulation.from(original)));
        Regulation restored = jsonMapper.readValue(jsonMapper.writeValueAsString(snapshot), RegulationSnapshot.class)
                .regulations().getFirst().toNewEntity();

        assertThat(restored.getSummary()).isEqualTo("Zakon uređuje testiranje.");
    }
}
