import type {FundingCallStatus} from "@/types/fundingCalls";

export type CitingFundingCall = {
    id: number;
    title: string;
    provider: string | null;
    status: FundingCallStatus | null;
    deadline: string | null;
};
