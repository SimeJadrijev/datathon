package com.datathon.api.ai;

import static org.assertj.core.api.Assertions.assertThat;

import com.datathon.api.fundingcall.Area;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class RegulationSummaryTest {

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    private RegulationSummary parse(String json) {
        return RegulationSummary.from(jsonMapper.readTree(json));
    }

    @Test
    void parsesValidAnswer() {
        RegulationSummary summary = parse("""
                {"summary":"Zakon uređuje obrte i stupa na snagu 1. siječnja 2027.",
                 "areas":["ENTREPRENEURSHIP","EMPLOYMENT","EDUCATION","HEALTH"]}""");

        assertThat(summary.summary()).startsWith("Zakon uređuje obrte");
        assertThat(summary.areas()).hasSize(3).contains(Area.ENTREPRENEURSHIP);
    }

    @Test
    void nullSummaryStaysNull() {
        RegulationSummary summary = parse("{\"summary\":null,\"areas\":[]}");

        assertThat(summary.summary()).isNull();
        assertThat(summary.areas()).isEmpty();
    }

    @Test
    void tooShortSummaryAndUnknownAreaAreDropped() {
        RegulationSummary summary = parse("{\"summary\":\"x\",\"areas\":[\"NOPE\",\"ENERGY\"]}");

        assertThat(summary.summary()).isNull();
        assertThat(summary.areas()).containsExactly(Area.ENERGY);
    }
}
