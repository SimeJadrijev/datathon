export type SourceCode =
    | "ASSOCIATIONS_OFFICE"
    | "FZOEU"
    | "EU_FUNDS_NPOO"
    | "EU_FUNDS_COHESION"
    | "GOV_HR_NEWS"
    | "LOCAL_GOVERNMENT"
    | "AGENCIES"
    | "UNIVERSITIES"
    | "HZZ"
    | "APPRRR"
    | "MZOM"
    | "NARODNE_NOVINE"
    | "SUDREG"
    | "ASSOCIATION_REGISTRY";

export type SourceMethod = "API" | "HTML" | "PDF";

export type SourceRef = {
    code: SourceCode;
    name: string;
};

export type SourceInfo = SourceRef & {
    url: string;
};

export type SourceSummary = SourceInfo & {
    description: string;
    method: SourceMethod;
    license: string;
    recordCount: number | null;
    lastSuccessfulFetch: string | null;
};
