import {keepPreviousData, useQuery} from "@tanstack/react-query";
import {getFundingCallFacets} from "@/api";
import type {FundingCallFilter} from "@/types";
import {fundingCallKeys} from "./fundingCallKeys.ts";

export const useGetFundingCallFacets = (filter: FundingCallFilter) =>
    useQuery({
        queryKey: fundingCallKeys.facets(filter),
        queryFn: () => getFundingCallFacets(filter),
        placeholderData: keepPreviousData,
    });
