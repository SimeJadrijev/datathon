import type {TargetGroup} from "@/types/codeLists";

export type EligibilityQuestionType = "YES_NO" | "NUMBER" | "CHOICE";

export type EligibilityQuestion = {
    id: string;
    text: string;
    type: EligibilityQuestionType;
    options: string[] | null;
};

export type EligibilityAnswer = boolean | number | string;

export type EligibilityCheckRequest = {
    answers: Record<string, EligibilityAnswer>;
    targetGroup?: TargetGroup;
    county?: string;
};

export type EligibilityVerdict = "LIKELY_ELIGIBLE" | "LIKELY_NOT_ELIGIBLE" | "UNCLEAR";

export type EligibilityReason = {
    text: string;
    quote: string | null;
};

export type EligibilityCheckResponse = {
    verdict: EligibilityVerdict;
    reasons: EligibilityReason[];
    missingInfo: string[];
    disclaimer: string;
    sourceUrl: string | null;
};
