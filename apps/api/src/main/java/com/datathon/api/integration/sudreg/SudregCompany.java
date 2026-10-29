package com.datathon.api.integration.sudreg;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
record SudregCompany(
        @JsonProperty("tvrtka") Name name,
        @JsonProperty("sjediste") Seat seat,
        @JsonProperty("pravni_oblik") LegalForm legalForm,
        @JsonProperty("glavna_djelatnost") Integer mainActivity) {

    private static final int NKD_DIGITS = 4;

    boolean hasName() {
        return name != null && name.name() != null && !name.name().isBlank();
    }

    CompanyInfo toCompanyInfo() {
        String legalFormAbbreviation = legalForm == null || legalForm.type() == null
                ? null
                : legalForm.type().abbreviation();
        return new CompanyInfo(
                name == null ? null : name.name(),
                new CompanyInfo.Seat(
                        seat == null ? null : seat.municipality(),
                        seat == null ? null : seat.county()),
                legalFormAbbreviation,
                toActivityCode(mainActivity));
    }

    /** Sudreg stores the NKD code as a number without the dot: 6201 is 62.01 and 150 is 01.50. */
    static String toActivityCode(Integer mainActivity) {
        if (mainActivity == null || mainActivity == 0) {
            return null;
        }
        String digits = String.format("%0" + NKD_DIGITS + "d", mainActivity);
        return digits.substring(0, 2) + "." + digits.substring(2);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Name(@JsonProperty("ime") String name) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Seat(
            @JsonProperty("naziv_opcine") String municipality,
            @JsonProperty("naziv_zupanije") String county) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record LegalForm(@JsonProperty("vrsta_pravnog_oblika") LegalFormType type) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record LegalFormType(@JsonProperty("kratica") String abbreviation) {
    }
}
