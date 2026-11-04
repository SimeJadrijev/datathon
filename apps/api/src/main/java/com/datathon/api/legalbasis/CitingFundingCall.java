package com.datathon.api.legalbasis;

import com.datathon.api.fundingcall.FundingCall;
import com.datathon.api.fundingcall.FundingCallStatus;
import java.time.LocalDate;

public record CitingFundingCall(Long id, String title, String provider, FundingCallStatus status, LocalDate deadline) {

    static CitingFundingCall from(FundingCall fundingCall) {
        return new CitingFundingCall(fundingCall.getId(), fundingCall.getTitle(), fundingCall.getProvider(),
                fundingCall.getStatus(), fundingCall.getDeadline());
    }
}
