import {api} from "@/api/base.ts";
import type {FundingCallFilter, FundingCallSummary, Page} from "@/types";

export const getFundingCalls = (params: FundingCallFilter) =>
    api.get<Page<FundingCallSummary>, Page<FundingCallSummary>>("/funding-calls", {params});
