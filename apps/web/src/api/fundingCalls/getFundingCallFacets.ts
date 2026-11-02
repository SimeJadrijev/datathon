import {api} from "@/api/base.ts";
import type {FundingCallFacets, FundingCallFilter} from "@/types";

export const getFundingCallFacets = (filter: FundingCallFilter) =>
    api.get<FundingCallFacets, FundingCallFacets>("/funding-calls/facets", {params: filter});
