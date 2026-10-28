package com.datathon.api.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class OibValidatorTest {

    private final OibValidator validator = new OibValidator();

    @ParameterizedTest
    @ValueSource(strings = {"21173725829", "36201212847", "94989605030", "12345678903", "55555555551"})
    void acceptsOibsWithACorrectCheckDigit(String oib) {
        assertThat(validator.isValid(oib, null)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"21173725828", "12345678900", "2117372582", "211737258290", "2117372582a", "abcdefghijk", "21173 25829"})
    void rejectsWrongCheckDigitWrongLengthAndNonDigits(String oib) {
        assertThat(validator.isValid(oib, null)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"٢١١٧٣٧٢٥٨٢٩"})
    void rejectsNonAsciiDigits(String oib) {
        assertThat(validator.isValid(oib, null)).isFalse();
    }
}
