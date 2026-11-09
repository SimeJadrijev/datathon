import type {Area, TargetGroup} from "@/types/codeLists";
import type {SourceCode} from "@/types/sources";
import type {FundingCallStatus, FundingCallType} from "./FundingCall.type.ts";

export type FundingCallStatusFilter = FundingCallStatus | "UNKNOWN";

export type FundingCallSort = "deadline" | "publishedAt" | "newest";

export type FundingCallFilter = {
    q?: string;
    status?: FundingCallStatusFilter;
    targetGroup?: TargetGroup;
    area?: Area;
    county?: string;
    source?: SourceCode;
    type?: FundingCallType;
    deadlineBefore?: string;
    sort?: FundingCallSort;
    page?: number;
    size?: number;
};
