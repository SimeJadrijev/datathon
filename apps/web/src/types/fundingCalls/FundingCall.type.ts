import type {SourceRef} from "@/types/sources";
import type {Area, TargetGroup} from "@/types/codeLists";

export type FundingCallType = "GRANT" | "SUBSIDY" | "PUBLIC_CALL" | "SCHOLARSHIP" | "LOAN" | "AWARD";

export type FundingCallStatus = "ANNOUNCED" | "OPEN" | "CLOSED";

// Mirrors FundingCallSummary from GET /api/funding-calls and POST /api/profiles/match.
export type FundingCallSummary = {
    id: number;
    title: string;
    provider: string | null;
    source: SourceRef;
    type: FundingCallType | null;
    status: FundingCallStatus | null;
    deadline: string | null;
    deadlineNote: string | null;
    totalAmount: number | null;
    maxAmount: number | null;
    targetGroups: TargetGroup[];
    areas: Area[];
    counties: string[];
    extractedByLlm: boolean;
    url: string;
};
