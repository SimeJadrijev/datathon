import type {FundingCallFilter} from "@/types";

export const fundingCallKeys = {
    all: ["funding-calls"] as const,
    list: (filter: FundingCallFilter) => [...fundingCallKeys.all, "list", filter] as const,
};
