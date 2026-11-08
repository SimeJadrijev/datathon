package com.datathon.api.common;

import static org.assertj.core.api.Assertions.assertThat;

import com.datathon.api.fundingcall.FundingCall;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CountyNamesTest {

    @Test
    void everySpellingOfACountyBecomesTheShortName() {
        assertThat(CountyNames.canonical("Istarska županija")).isEqualTo("Istarska");
        assertThat(CountyNames.canonical("PRIMORSKO-GORANSKA")).isEqualTo("Primorsko-goranska");
        assertThat(CountyNames.canonical("Medimurska")).isEqualTo("Međimurska");
        assertThat(CountyNames.canonical(" Grad Zagreb ")).isEqualTo("Grad Zagreb");
        assertThat(CountyNames.canonical("Zagrebačka županija")).isEqualTo("Zagrebačka");
    }

    @Test
    void wholeCountryBecomesTheMarkerAndOtherPlacesStay() {
        assertThat(CountyNames.canonical("Područje cijele Hrvatske")).isEqualTo(FundingCall.ALL_COUNTIES);
        assertThat(CountyNames.canonical("Izvan Republike Hrvatske")).isEqualTo("Izvan Republike Hrvatske");
    }

    @Test
    void wholeCountryWinsOverSingleCountiesAndBlanksAreDropped() {
        assertThat(CountyNames.canonical(List.of("Istarska županija", "Istarska", " ")))
                .containsExactly("Istarska");
        assertThat(CountyNames.canonical(List.of("Istarska", "sve"))).containsExactly(FundingCall.ALL_COUNTIES);
        assertThat(CountyNames.canonical((Set<String>) null)).isEmpty();
    }

    @Test
    void nationalCallsAreOfferedInACountyButNotAbroad() {
        List<String> stored = List.of("ALL", "Istarska", "Izvan Republike Hrvatske");

        assertThat(CountyNames.storedValuesMatching("Istarska županija", stored)).containsOnly("ALL", "Istarska");
        assertThat(CountyNames.storedValuesMatching("Izvan Republike Hrvatske", stored))
                .containsOnly("Izvan Republike Hrvatske");
    }
}
