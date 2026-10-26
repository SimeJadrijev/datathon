package com.datathon.api.seed;

import java.time.LocalDate;
import java.util.List;

/** Content of {@code seed/funding-calls.json}, written by {@code tools/export_poc_snapshot.py}. */
public record FundingCallSnapshot(LocalDate snapshotDate, List<SeedFundingCall> fundingCalls) {
}
