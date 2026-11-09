import {api} from "@/api/base.ts";
import type {EligibilityCheckRequest, EligibilityCheckResponse} from "@/types";

interface EligibilityCheckParams {
    fundingCallId: number;
    request: EligibilityCheckRequest;
}

export const getEligibilityCheck = ({fundingCallId, request}: EligibilityCheckParams) =>
    api.post<EligibilityCheckResponse, EligibilityCheckResponse>(
        `/funding-calls/${fundingCallId}/eligibility-check`,
        request,
    );
