package com.datathon.api.seed;

import java.time.LocalDate;
import java.util.List;

public record RegulationSnapshot(LocalDate snapshotDate, List<SeedRegulation> regulations) {
}
