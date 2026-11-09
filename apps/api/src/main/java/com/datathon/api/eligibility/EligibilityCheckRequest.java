package com.datathon.api.eligibility;

import com.datathon.api.fundingcall.TargetGroup;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Map;

public record EligibilityCheckRequest(
        @NotNull(message = "odgovori nedostaju") @Size(max = 10, message = "smije imati najviše 10 odgovora")
        Map<String, Object> answers,
        TargetGroup targetGroup,
        @Size(max = 100, message = "smije imati najviše 100 znakova") String county) {
}
