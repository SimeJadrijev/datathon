package com.datathon.api.integration.sudreg;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import tools.jackson.databind.json.JsonMapper;

class SudregCompanyTest {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    @Test
    void mapsNameSeatLegalFormAndActivityOfAnItCompany() throws IOException {
        CompanyInfo company = read("21173725829.json").toCompanyInfo();

        assertThat(company.name()).isEqualTo("Microblink društvo s ograničenom odgovornošću za informacijske tehnologije");
        assertThat(company.seat().municipality()).isEqualTo("Zagreb");
        assertThat(company.seat().county()).isEqualTo("Grad Zagreb");
        assertThat(company.legalForm()).isEqualTo("d.o.o.");
        assertThat(company.activityCode()).isEqualTo("62.01");
    }

    @Test
    void mapsAJointStockCompany() throws IOException {
        CompanyInfo company = read("36201212847.json").toCompanyInfo();

        assertThat(company.legalForm()).isEqualTo("d.d.");
        assertThat(company.activityCode()).isEqualTo("55.10");
    }

    @Test
    void codeWithLeadingZeroKeepsItWhenTheDotIsRestored() throws IOException {
        CompanyInfo company = read("leading-zero-activity.json").toCompanyInfo();

        assertThat(company.activityCode()).isEqualTo("01.50");
        assertThat(company.seat().county()).isEqualTo("Vukovarsko-srijemska");
    }

    @Test
    void convertsRegisterActivityNumbersToNkdCodes() {
        assertThat(SudregCompany.toActivityCode(1072)).isEqualTo("10.72");
        assertThat(SudregCompany.toActivityCode(150)).isEqualTo("01.50");
        assertThat(SudregCompany.toActivityCode(6201)).isEqualTo("62.01");
        assertThat(SudregCompany.toActivityCode(0)).isNull();
        assertThat(SudregCompany.toActivityCode(null)).isNull();
    }

    @Test
    void missingPartsGiveEmptyFieldsInsteadOfAFailure() {
        CompanyInfo company = new SudregCompany(null, null, null, null).toCompanyInfo();

        assertThat(company.name()).isNull();
        assertThat(company.seat()).isEqualTo(new CompanyInfo.Seat(null, null));
        assertThat(company.legalForm()).isNull();
        assertThat(company.activityCode()).isNull();
    }

    @Test
    void answerWithoutACompanyNameIsNotACompany() throws IOException {
        assertThat(new SudregCompany(null, null, null, 6201).hasName()).isFalse();
        assertThat(read("21173725829.json").hasName()).isTrue();
    }

    private static SudregCompany read(String fixture) throws IOException {
        try (InputStream input = new ClassPathResource("fixtures/sudreg/" + fixture).getInputStream()) {
            return JSON.readValue(input, SudregCompany.class);
        }
    }
}
