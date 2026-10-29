import {keepPreviousData, useQuery} from "@tanstack/react-query";
import {getFundingCalls} from "@/api";
import type {FundingCallFilter} from "@/types";
import {fundingCallKeys} from "./fundingCallKeys.ts";

export const useGetFundingCalls = (filter: FundingCallFilter) =>
    useQuery({
        queryKey: fundingCallKeys.list(filter),
        queryFn: () => getFundingCalls(filter),
        placeholderData: keepPreviousData,
    });
