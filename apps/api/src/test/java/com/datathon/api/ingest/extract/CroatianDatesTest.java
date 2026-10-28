package com.datathon.api.ingest.extract;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class CroatianDatesTest {

    @Test
    void readsNumericAndWrittenDates() {
        assertThat(CroatianDates.findAll("od 28.10.2026. do 28. 11. 2026, a najkasnije 15. prosinca 2026.")
                .stream().map(CroatianDates.DateMatch::date))
                .containsExactly(LocalDate.of(2026, 10, 28), LocalDate.of(2026, 11, 28), LocalDate.of(2026, 12, 15));
    }

    @Test
    void readsTwoDigitYearAndSkipsImpossibleDates() {
        assertThat(CroatianDates.findFirst("rok 31.12.26.")).contains(LocalDate.of(2026, 12, 31));
        assertThat(CroatianDates.findFirst("31.02.2026.")).isEmpty();
    }
}
