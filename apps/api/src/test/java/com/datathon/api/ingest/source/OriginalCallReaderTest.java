package com.datathon.api.ingest.source;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.datathon.api.ingest.RawAttachment;
import com.datathon.api.ingest.extract.PdfTextExtractor;
import com.datathon.api.ingest.http.PoliteHttpClient;
import com.datathon.api.ingest.http.RobotsBlockedException;
import org.junit.jupiter.api.Test;

class OriginalCallReaderTest {

    private static final String PAGE = """
            <html><body>
            <nav><a href="/">Naslovnica</a> Izbornik grada</nav>
            <main>
              <h1>Javni poziv za udruge</h1>
              <p>Rok za podnošenje prijava je 15. prosinca 2026.</p>
              <a href="/dokumenti/javni-poziv.pdf">Tekst javnog poziva</a>
            </main>
            <footer>Grad Našice, sva prava pridržana</footer>
            </body></html>""";

    private final PoliteHttpClient http = mock(PoliteHttpClient.class);
    private final OriginalCallReader reader = new OriginalCallReader(http, new PdfTextExtractor(http));

    @Test
    void readsTheMainTextOfTheCityPageAndItsDocuments() {
        when(http.getText("https://nasice.hr/javni-poziv")).thenReturn(PAGE);
        when(http.getBytes("https://nasice.hr/dokumenti/javni-poziv.pdf")).thenReturn(new byte[0]);

        String text = reader.read(new RawAttachment("Izvorni natječaj/poziv", "https://nasice.hr/javni-poziv#:~:text=x"),
                "udruge.gov.hr");

        assertThat(text).startsWith(OriginalCallReader.PAGE_HEADER)
                .contains("Rok za podnošenje prijava je 15. prosinca 2026.")
                .contains("=== Tekst javnog poziva ===")
                .doesNotContain("Izbornik grada", "sva prava pridržana");
    }

    @Test
    void pageBlockedByRobotsGivesNoText() {
        when(http.getText(anyString())).thenThrow(new RobotsBlockedException("https://split.hr/x"));

        assertThat(reader.read(new RawAttachment("Izvorni natječaj/poziv", "https://split.hr/x"), "udruge.gov.hr"))
                .isEmpty();
    }

    @Test
    void linkBackToTheSameSourceIsNotFetched() {
        String text = reader.read(new RawAttachment("Izvorni natječaj/poziv",
                "https://udruge.gov.hr/istaknute-teme/program"), "udruge.gov.hr");

        assertThat(text).isEmpty();
        verify(http, never()).getText(anyString());
    }
}
