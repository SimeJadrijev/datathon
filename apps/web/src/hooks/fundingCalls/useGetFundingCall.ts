import {useQuery} from "@tanstack/react-query";
import {getFundingCall} from "@/api";
import {fundingCallKeys} from "./fundingCallKeys.ts";

export const useGetFundingCall = (id: number) =>
    useQuery({
        queryKey: fundingCallKeys.detail(id),
        queryFn: () => getFundingCall(id),
    });
