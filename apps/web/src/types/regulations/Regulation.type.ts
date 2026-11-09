import type {Area} from "@/types/codeLists";
import type {CitingFundingCall} from "./CitingFundingCall.type.ts";

export type RegulationType = "LAW" | "DECREE" | "ORDINANCE" | "DECISION" | "OTHER";

export type RegulationSummary = {
    id: number;
    title: string;
    type: RegulationType | null;
    gazetteNumber: string | null;
    publishedAt: string | null;
    areas: Area[];
    hasSummary: boolean;
};

export type RegulationDetail = Omit<RegulationSummary, "hasSummary"> & {
    documentNumber: number | null;
    enactedAt: string | null;
    summary: string | null;
    url: string | null;
    eliUrl: string | null;
    pdfUrl: string | null;
    eliMetadata: boolean;
    fetchedAt: string | null;
    citingFundingCalls: CitingFundingCall[];
};
