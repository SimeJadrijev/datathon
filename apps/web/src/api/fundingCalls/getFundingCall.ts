import {api} from "@/api/base.ts";
import type {FundingCallDetail} from "@/types";

export const getFundingCall = (id: number) =>
    api.get<FundingCallDetail, FundingCallDetail>(`/funding-calls/${id}`);
