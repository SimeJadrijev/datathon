package com.datathon.api.meta;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class SnapshotDateTest {

    @Test
    void readsTheDateFromTheSeedFile() {
        var snapshotDate = new SnapshotDate(JsonMapper.builder().build());

        assertThat(snapshotDate.value()).contains(LocalDate.of(2026, 11, 3));
    }
}
