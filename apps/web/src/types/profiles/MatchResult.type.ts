import type {FundingCallSummary} from "@/types/fundingCalls";

export type MatchResult = {
    fundingCall: FundingCallSummary;
    score: number;
    reasons: string[];
    statusUnknown: boolean;
};
