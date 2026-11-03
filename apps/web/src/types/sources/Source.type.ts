export type SourceCode =
    | "ASSOCIATIONS_OFFICE"
    | "FZOEU"
    | "EU_FUNDS_NPOO"
    | "EU_FUNDS_COHESION"
    | "HZZ"
    | "APPRRR"
    | "MZOM"
    | "NARODNE_NOVINE"
    | "SUDREG";

export type SourceMethod = "API" | "HTML" | "PDF";

export type SourceRef = {
    code: SourceCode;
    name: string;
};

export type SourceInfo = SourceRef & {
    url: string;
};

// Mirrors SourceSummary from GET /api/sources.
export type SourceSummary = SourceInfo & {
    description: string;
    method: SourceMethod;
    license: string;
    recordCount: number | null;
    lastSuccessfulFetch: string | null;
};
