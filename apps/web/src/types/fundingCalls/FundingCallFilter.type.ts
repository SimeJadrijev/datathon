import type {FundingCallStatus} from "./FundingCall.type.ts";

export type FundingCallSort = "deadline" | "publishedAt";

// Query parameters of GET /api/funding-calls (FundingCallFilter in the API). Only the ones the web uses so far.
export type FundingCallFilter = {
    status?: FundingCallStatus;
    deadlineBefore?: string;
    sort?: FundingCallSort;
    page?: number;
    size?: number;
};
