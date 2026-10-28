package com.datathon.api.profile;

import com.datathon.api.fundingcall.dto.FundingCallSummary;
import java.util.List;

public record MatchResult(FundingCallSummary fundingCall, double score, List<String> reasons, boolean statusUnknown) {
}
