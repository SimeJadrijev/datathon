import {api} from "@/api/base.ts";
import type {EligibilityQuestion} from "@/types";

export const getEligibilityQuestions = (fundingCallId: number) =>
    api.get<EligibilityQuestion[], EligibilityQuestion[]>(`/funding-calls/${fundingCallId}/eligibility-questions`);
