import type {FundingCallFilter} from "@/types";

export const fundingCallKeys = {
    all: ["funding-calls"] as const,
    list: (filter: FundingCallFilter) => [...fundingCallKeys.all, "list", filter] as const,
    facets: (filter: FundingCallFilter) => [...fundingCallKeys.all, "facets", filter] as const,
    detail: (id: number) => [...fundingCallKeys.all, "detail", id] as const,
};
