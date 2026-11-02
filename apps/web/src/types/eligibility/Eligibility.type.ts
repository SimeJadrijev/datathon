import type {TargetGroup} from "@/types/codeLists";

export type EligibilityQuestionType = "YES_NO" | "NUMBER" | "CHOICE";

// Mirrors EligibilityQuestion from GET /api/funding-calls/{id}/eligibility-questions. options only for CHOICE.
export type EligibilityQuestion = {
    id: string;
    text: string;
    type: EligibilityQuestionType;
    options: string[] | null;
};

export type EligibilityAnswer = boolean | number | string;

// Mirrors EligibilityCheckRequest of POST /api/funding-calls/{id}/eligibility-check.
export type EligibilityCheckRequest = {
    answers: Record<string, EligibilityAnswer>;
    targetGroup?: TargetGroup;
    county?: string;
};

export type EligibilityVerdict = "LIKELY_ELIGIBLE" | "LIKELY_NOT_ELIGIBLE" | "UNCLEAR";

// quote is a literal passage from the call text; null only for rule-based reasons.
export type EligibilityReason = {
    text: string;
    quote: string | null;
};

// Mirrors EligibilityCheckResponse.
export type EligibilityCheckResponse = {
    verdict: EligibilityVerdict;
    reasons: EligibilityReason[];
    missingInfo: string[];
    disclaimer: string;
    sourceUrl: string;
};
