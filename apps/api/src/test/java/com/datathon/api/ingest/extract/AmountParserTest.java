package com.datathon.api.ingest.extract;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class AmountParserTest {

    @Test
    void picksTotalAndMaximumByContext() {
        var amounts = AmountParser.pick(List.of(
                "Ukupno raspoloživa sredstva iznose 1.500.000,00 eura. Najviši iznos po prijavitelju je 20.000 EUR, "
                        + "a najniži iznos 2.000 €."));

        assertThat(amounts.total()).isEqualByComparingTo("1500000");
        assertThat(amounts.max()).isEqualByComparingTo("20000");
        assertThat(amounts.min()).isEqualByComparingTo("2000");
    }

    @Test
    void readsMillionsAndIgnoresSmallNumbers() {
        var amounts = AmountParser.pick(List.of("Alokacija poziva je 3,5 milijuna eura. Pristojba 10 eura."));

        assertThat(amounts.total()).isEqualByComparingTo("3500000");
        assertThat(amounts.max()).isNull();
    }

    @Test
    void parsesStructuredFieldWithOrWithoutCurrency() {
        assertThat(AmountParser.parseStructured("50.000,00 EUR")).contains(new BigDecimal("50000.00"));
        assertThat(AmountParser.parseStructured("€ 7.500")).contains(new BigDecimal("7500"));
        assertThat(AmountParser.parseStructured("12500")).contains(new BigDecimal("12500"));
        assertThat(AmountParser.parseStructured("nije navedeno")).isEmpty();
    }
}
