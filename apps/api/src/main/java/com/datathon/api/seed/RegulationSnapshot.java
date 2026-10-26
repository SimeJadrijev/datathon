package com.datathon.api.seed;

import java.time.LocalDate;
import java.util.List;

/** Content of {@code seed/regulations.json}, written by {@code tools/export_poc_snapshot.py}. */
public record RegulationSnapshot(LocalDate snapshotDate, List<SeedRegulation> regulations) {
}
