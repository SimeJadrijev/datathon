import type {SourceInfo} from "@/types/sources";
import type {FundingCallSummary} from "./FundingCall.type.ts";
import type {LegalBasis} from "./LegalBasis.type.ts";

export type Attachment = {
    name: string | null;
    url: string;
};

// Mirrors FundingCallDetail from GET /api/funding-calls/{id}. aiFields names the fields the model filled in.
export type FundingCallDetail = Omit<FundingCallSummary, "source" | "firstSeenAt"> & {
    source: SourceInfo;
    publishedAt: string | null;
    minAmount: number | null;
    conditionsSummary: string | null;
    requiredDocuments: string[];
    attachments: Attachment[];
    legalBasis: LegalBasis[];
    fieldsReviewed: boolean;
    fetchedAt: string | null;
};
