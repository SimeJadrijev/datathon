import type {Area} from "./Area.type.ts";
import type {SourceRef} from "./Source.type.ts";
import type {TargetGroup} from "./TargetGroup.type.ts";

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
