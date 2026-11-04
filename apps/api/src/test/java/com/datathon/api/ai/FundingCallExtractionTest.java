package com.datathon.api.ai;

import static org.assertj.core.api.Assertions.assertThat;

import com.datathon.api.fundingcall.Area;
import com.datathon.api.fundingcall.FundingCall;
import com.datathon.api.fundingcall.FundingCallStatus;
import com.datathon.api.fundingcall.FundingCallType;
import com.datathon.api.fundingcall.TargetGroup;
import com.datathon.api.source.SourceCode;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class FundingCallExtractionTest {

    private final JsonMapper jsonMapper = JsonMapper.builder().build();
    private final FundingCallExtractionMerger merger = new FundingCallExtractionMerger();

    private FundingCallExtraction parse(String json) {
        return FundingCallExtraction.from(jsonMapper.readTree(json));
    }

    private FundingCall ruleBasedCall() {
        FundingCall call = new FundingCall(SourceCode.FZOEU, "275", "Electric cars");
        call.setStatus(FundingCallStatus.OPEN);
        call.setDeadline(LocalDate.of(2028, 3, 31));
        call.setMaxAmount(new BigDecimal("2500"));
        call.setTotalAmount(new BigDecimal("20000000"));
        call.getTargetGroups().add(TargetGroup.COMPANY);
        return call;
    }

    @Test
    void parsesValidAnswer() {
        FundingCallExtraction extraction = parse("""
                {"provider":"FZOEU","type":"SUBSIDY","publishedAt":"2026-09-01","deadline":null,
                 "totalAmount":20000000,"minAmount":0,"maxAmount":9000,
                 "targetGroups":["CITIZEN"],"areas":["ENERGY","TRANSPORT","ENVIRONMENT","HEALTH"],
                 "activityCodes":[],"counties":["ALL"],
                 "conditionsSummary":"Punoljetne fizičke osobe mogu dobiti potporu za električno vozilo.",
                 "requiredDocuments":["Prijavni obrazac"]}""");

        assertThat(extraction.type()).isEqualTo(FundingCallType.SUBSIDY);
        assertThat(extraction.deadline()).isNull();
        assertThat(extraction.minAmount()).isNull();
        assertThat(extraction.maxAmount()).isEqualByComparingTo("9000");
        assertThat(extraction.areas()).hasSize(3);
        assertThat(extraction.counties()).containsExactly(FundingCall.ALL_COUNTIES);
    }

    @Test
    void dropsUnknownEnumAndShortSummary() {
        FundingCallExtraction extraction = parse("""
                {"type":"WHATEVER","targetGroups":["COMPANY","ALIENS"],"areas":[],"conditionsSummary":"x"}""");

        assertThat(extraction.type()).isNull();
        assertThat(extraction.targetGroups()).containsExactly(TargetGroup.COMPANY);
        assertThat(extraction.conditionsSummary()).isNull();
    }

    @Test
    void modelValueReplacesRuleValueOnlyWhenPresent() {
        FundingCall call = ruleBasedCall();

        merger.apply(call, parse("{\"maxAmount\":9000,\"totalAmount\":null,\"targetGroups\":[]}"));

        assertThat(call.getMaxAmount()).isEqualByComparingTo("9000");
        assertThat(call.getTotalAmount()).isEqualByComparingTo("20000000");
        assertThat(call.getTargetGroups()).containsExactly(TargetGroup.COMPANY);
        assertThat(call.getAiFields()).contains("maxAmount").doesNotContain("totalAmount", "targetGroups");
        assertThat(call.isExtractedByLlm()).isTrue();
    }

    @Test
    void nullDeadlineFromModelClearsRuleDeadline() {
        FundingCall call = ruleBasedCall();

        merger.apply(call, parse("{\"deadline\":null,\"areas\":[\"ENERGY\"]}"));

        assertThat(call.getDeadline()).isNull();
        assertThat(call.getAiFields()).contains("deadline", "areas");
        assertThat(call.getAreas()).containsExactly(Area.ENERGY);
    }

    @Test
    void deadlineFromStructuredSourceDataIsKeptOverTheModel() {
        FundingCall call = ruleBasedCall();
        call.setDeadlineFromSource(true);

        merger.apply(call, parse("{\"deadline\":\"2020-01-01\"}"));

        assertThat(call.getDeadline()).isEqualTo(LocalDate.of(2028, 3, 31));
        assertThat(call.getStatus()).isEqualTo(FundingCallStatus.OPEN);
        assertThat(call.getAiFields()).doesNotContain("deadline");
    }

    @Test
    void clearedDeadlineGetsTheUntilFundsRunOutNote() {
        FundingCall call = ruleBasedCall();
        call.setSourceText("Poziv traje do isteka godine ili do iskorištenja sredstava.");

        merger.apply(call, parse("{\"deadline\":null}"));

        assertThat(call.getDeadline()).isNull();
        assertThat(call.getDeadlineNote()).isEqualTo("do iskorištenja sredstava");
    }

    @Test
    void countyOfALocalSourceIsNotWidenedToTheWholeCountry() {
        FundingCall call = ruleBasedCall();
        call.getCounties().add("Karlovačka");

        merger.apply(call, parse("{\"counties\":[\"ALL\"]}"));

        assertThat(call.getCounties()).containsExactly("Karlovačka");
        assertThat(call.getAiFields()).doesNotContain("counties");
    }

    @Test
    void nationalCallCanBeNarrowedByTheModel() {
        FundingCall call = ruleBasedCall();
        call.getCounties().add(FundingCall.ALL_COUNTIES);

        merger.apply(call, parse("{\"counties\":[\"Splitsko-dalmatinska\"]}"));

        assertThat(call.getCounties()).containsExactly("Splitsko-dalmatinska");
    }

    @Test
    void deadlineInThePastClosesTheCall() {
        FundingCall call = ruleBasedCall();

        merger.apply(call, parse("{\"deadline\":\"2020-01-01\"}"));

        assertThat(call.getStatus()).isEqualTo(FundingCallStatus.CLOSED);
    }

    @Test
    void reviewedCallKeepsHandCheckedFieldsAndGetsOnlyTheTexts() {
        FundingCall call = ruleBasedCall();
        call.setFieldsReviewed(true);
        LocalDate reviewedDeadline = call.getDeadline();

        merger.apply(call, parse("""
                {"deadline":null,"maxAmount":1,"targetGroups":["CITIZEN"],
                 "conditionsSummary":"Mogu se prijaviti trgovačka društva sa sjedištem u Hrvatskoj.",
                 "requiredDocuments":["Prijavni obrazac"]}"""));

        assertThat(call.getDeadline()).isEqualTo(reviewedDeadline);
        assertThat(call.getMaxAmount()).isNotEqualByComparingTo("1");
        assertThat(call.getTargetGroups()).containsExactly(TargetGroup.COMPANY);
        assertThat(call.getConditionsSummary()).startsWith("Mogu se prijaviti");
        assertThat(call.getRequiredDocuments()).containsExactly("Prijavni obrazac");
        assertThat(call.getAiFields()).containsExactlyInAnyOrder("conditionsSummary", "requiredDocuments");
        assertThat(call.isExtractedByLlm()).isTrue();
    }
}
