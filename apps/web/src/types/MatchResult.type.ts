import type {FundingCallSummary} from "./FundingCall.type.ts";

// Mirrors MatchResult from POST /api/profiles/match (API-6).
export type MatchResult = {
    fundingCall: FundingCallSummary;
    score: number;
    reasons: string[];
    statusUnknown: boolean;
};
