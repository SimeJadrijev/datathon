package com.datathon.api.ingest.extract;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class DeadlineFinderTest {

    private static final LocalDate PUBLISHED = LocalDate.of(2026, 3, 2);

    @Test
    void findsExplicitApplicationDeadline() {
        String text = "Rok za podnošenje prijava je 15. svibnja 2026. godine do 16:00 sati.";

        assertThat(DeadlineFinder.find(text, PUBLISHED)).contains(LocalDate.of(2026, 5, 15));
    }

    @Test
    void ignoresPublicationDateAfterKeyword() {
        String text = "Rok za podnošenje prijava: od 2. 3. 2026. do 30. 4. 2026.";

        assertThat(DeadlineFinder.find(text, PUBLISHED)).contains(LocalDate.of(2026, 4, 30));
    }

    @Test
    void doesNotTakeEligibilityPeriodOrPaymentDateAsDeadline() {
        String text = """
                Prihvatljivi i opravdani troškovi po Pozivu su isključivo troškovi:
                nabave novog vozila (radi stjecanja vlasništva ili financijskim leasingom) nastali
                najranije u razdoblju od dana objave ovog Poziva do 31. ožujka 2028. godine,
                Poziv traje do isteka kalendarske godine ili do iskorištenja sredstava, ovisno o tome
                što prije nastupi.
                Korisnik je dužan dostaviti zahtjev za isplatu s ispravnom dokumentacijom za isplatu
                najkasnije do 30. travnja 2028. godine.
                """;

        assertThat(DeadlineFinder.find(text, PUBLISHED)).isEmpty();
        assertThat(DeadlineFinder.findNote(text)).contains("do iskorištenja sredstava");
    }
}
