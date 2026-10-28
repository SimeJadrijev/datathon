package com.datathon.api.profile;

import com.datathon.api.fundingcall.FundingCall;
import java.util.List;

public record MatchedCall(FundingCall fundingCall, double score, List<String> reasons, boolean statusUnknown) {
}
