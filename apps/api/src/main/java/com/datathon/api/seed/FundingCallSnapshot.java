package com.datathon.api.seed;

import java.time.LocalDate;
import java.util.List;

public record FundingCallSnapshot(LocalDate snapshotDate, List<SeedFundingCall> fundingCalls) {
}
