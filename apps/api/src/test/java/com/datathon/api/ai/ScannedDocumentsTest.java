package com.datathon.api.ai;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ScannedDocumentsTest {

    @Test
    void findsOnlyDocumentSectionsWithoutText() {
        String sourceText = """
                Otvoren: 03.06.2026.

                === Javni poziv ZO-1/2026 ===



                === Upute za prijavitelje ===
                Rok za podnošenje prijava je 31. 12. 2026.
                === Obrazac 1 ===
                """;

        assertThat(ScannedDocuments.emptySections(sourceText))
                .containsExactly("Javni poziv ZO-1/2026", "Obrazac 1");
    }

    @Test
    void textWithoutDocumentsHasNoEmptySections() {
        assertThat(ScannedDocuments.emptySections("Institucija: Grad Našice")).isEmpty();
        assertThat(ScannedDocuments.emptySections(null)).isEmpty();
    }
}
