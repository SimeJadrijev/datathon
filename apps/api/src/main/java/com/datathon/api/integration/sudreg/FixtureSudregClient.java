package com.datathon.api.integration.sudreg;

import com.datathon.api.common.ExternalSourceException;
import com.datathon.api.common.NotFoundException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.core.io.ClassPathResource;
import tools.jackson.databind.json.JsonMapper;

public class FixtureSudregClient implements SudregClient {

    private static final String SOURCE = "Sudreg";
    private static final String FIXTURE_FOLDER = "sudreg-fixtures/";
    private static final int OIB_LENGTH = 11;

    private final JsonMapper jsonMapper;

    public FixtureSudregClient(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    @Override
    public CompanyInfo findByOib(String oib) {
        ClassPathResource fixture = new ClassPathResource(FIXTURE_FOLDER + oib + ".json");
        if (!isPlainOib(oib) || !fixture.exists()) {
            throw new NotFoundException("Tvrtka s ovim OIB-om nije pronađena.");
        }
        return toCompanyInfo(read(fixture));
    }

    private static boolean isPlainOib(String oib) {
        return oib.length() == OIB_LENGTH && oib.chars().allMatch(Character::isDigit);
    }

    private SudregCompany read(ClassPathResource fixture) {
        try (InputStream input = fixture.getInputStream()) {
            return jsonMapper.readValue(input, SudregCompany.class);
        } catch (IOException e) {
            throw new ExternalSourceException(SOURCE, "Sudreg fixture cannot be read");
        }
    }

    private static CompanyInfo toCompanyInfo(SudregCompany company) {
        String legalForm = company.legalForm() == null || company.legalForm().type() == null
                ? null
                : company.legalForm().type().abbreviation();
        return new CompanyInfo(
                company.name() == null ? null : company.name().name(),
                new CompanyInfo.Seat(
                        company.seat() == null ? null : company.seat().municipality(),
                        company.seat() == null ? null : company.seat().county()),
                legalForm,
                toActivityCode(company.mainActivity()));
    }

    /** Sudreg stores the NKD code as a number without the dot: 6201 is 62.01 and 150 is 01.50. */
    static String toActivityCode(Integer mainActivity) {
        if (mainActivity == null || mainActivity == 0) {
            return null;
        }
        String digits = String.format("%04d", mainActivity);
        return digits.substring(0, 2) + "." + digits.substring(2);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record SudregCompany(
            @JsonProperty("tvrtka") Name name,
            @JsonProperty("sjediste") Seat seat,
            @JsonProperty("pravni_oblik") LegalForm legalForm,
            @JsonProperty("glavna_djelatnost") Integer mainActivity) {

        @JsonIgnoreProperties(ignoreUnknown = true)
        private record Name(@JsonProperty("ime") String name) {
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        private record Seat(
                @JsonProperty("naziv_opcine") String municipality,
                @JsonProperty("naziv_zupanije") String county) {
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        private record LegalForm(@JsonProperty("vrsta_pravnog_oblika") LegalFormType type) {
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        private record LegalFormType(@JsonProperty("kratica") String abbreviation) {
        }
    }
}
