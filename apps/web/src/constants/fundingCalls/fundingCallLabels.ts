import type {FundingCallStatus, FundingCallType} from "@/types";

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
