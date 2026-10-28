package com.datathon.api.fundingcall.dto;

import com.datathon.api.fundingcall.FundingCallStatus;

public enum FundingCallStatusFilter {
    OPEN(FundingCallStatus.OPEN),
    ANNOUNCED(FundingCallStatus.ANNOUNCED),
    CLOSED(FundingCallStatus.CLOSED),
    UNKNOWN(null);

    private final FundingCallStatus status;

    FundingCallStatusFilter(FundingCallStatus status) {
        this.status = status;
    }

    public boolean isUnknown() {
        return status == null;
    }

    public FundingCallStatus status() {
        return status;
    }
}
