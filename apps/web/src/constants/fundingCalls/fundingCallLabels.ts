import type {FundingCallSort, FundingCallStatus, FundingCallStatusFilter, FundingCallType} from "@/types";

export const FUNDING_CALL_TYPES: FundingCallType[] = ["GRANT", "SUBSIDY", "PUBLIC_CALL", "SCHOLARSHIP", "LOAN", "AWARD"];

export const FUNDING_CALL_TYPE_LABELS: Record<FundingCallType, string> = {
    GRANT: "Bespovratna sredstva",
    SUBSIDY: "Potpora",
    PUBLIC_CALL: "Javni poziv",
    SCHOLARSHIP: "Stipendija",
    LOAN: "Zajam",
    AWARD: "Nagrada",
};

export const FUNDING_CALL_STATUS_LABELS: Record<FundingCallStatus, string> = {
    ANNOUNCED: "Najavljen",
    OPEN: "Otvoren",
    CLOSED: "Zatvoren",
};

// OPEN first: it is the API default, so it is never written into the URL.
export const FUNDING_CALL_STATUS_FILTERS: FundingCallStatusFilter[] = ["OPEN", "ANNOUNCED", "CLOSED", "UNKNOWN"];

export const DEFAULT_FUNDING_CALL_STATUS: FundingCallStatusFilter = "OPEN";

export const FUNDING_CALL_STATUS_FILTER_LABELS: Record<FundingCallStatusFilter, string> = {
    OPEN: "Otvoreni",
    ANNOUNCED: "Najavljeni",
    CLOSED: "Zatvoreni",
    UNKNOWN: "Status nepoznat",
};

export const FUNDING_CALL_SORTS: FundingCallSort[] = ["deadline", "publishedAt"];

export const DEFAULT_FUNDING_CALL_SORT: FundingCallSort = "deadline";

export const FUNDING_CALL_SORT_LABELS: Record<FundingCallSort, string> = {
    deadline: "roku prijave",
    publishedAt: "datumu objave",
};

export const isFundingCallType = (value: unknown): value is FundingCallType =>
    FUNDING_CALL_TYPES.some((type) => type === value);

export const isFundingCallStatusFilter = (value: unknown): value is FundingCallStatusFilter =>
    FUNDING_CALL_STATUS_FILTERS.some((status) => status === value);

export const isFundingCallSort = (value: unknown): value is FundingCallSort =>
    FUNDING_CALL_SORTS.some((sort) => sort === value);
