package com.datathon.api.source;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class SourceCatalogTest {

    private final SourceCatalog catalog = new SourceCatalog(JsonMapper.builder().build());

    @Test
    void containsEverySourceCodeExactlyOnce() {
        assertThat(catalog.entries()).extracting(SourceEntry::code)
                .containsExactlyInAnyOrder(SourceCode.values())
                .doesNotHaveDuplicates();
    }

    @Test
    void everyEntryIsFullyDescribed() {
        assertThat(catalog.entries()).allSatisfy(entry -> {
            assertThat(entry.name()).isNotBlank();
            assertThat(entry.description()).isNotBlank();
            assertThat(entry.url()).startsWith("https://");
            assertThat(entry.method()).isNotNull();
            assertThat(entry.license()).isNotBlank();
        });
    }

    @Test
    void infoLookupWorksForEverySourceCode() {
        Arrays.stream(SourceCode.values())
                .forEach(code -> assertThat(catalog.get(code).code()).isEqualTo(code));
    }
}
