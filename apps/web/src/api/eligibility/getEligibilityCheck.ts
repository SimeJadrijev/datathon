import {api} from "@/api/base.ts";
import type {EligibilityCheckRequest, EligibilityCheckResponse} from "@/types";

interface EligibilityCheckParams {
    fundingCallId: number;
    request: EligibilityCheckRequest;
}

// A POST that only reads: it judges the answers and saves nothing.
export const getEligibilityCheck = ({fundingCallId, request}: EligibilityCheckParams) =>
    api.post<EligibilityCheckResponse, EligibilityCheckResponse>(
        `/funding-calls/${fundingCallId}/eligibility-check`,
        request,
    );
