import type {SourceCode, SourceMethod} from "@/types";

export const SOURCE_CODES: SourceCode[] = [
    "ASSOCIATIONS_OFFICE",
    "FZOEU",
    "EU_FUNDS_NPOO",
    "EU_FUNDS_COHESION",
    "HZZ",
    "APPRRR",
    "MZOM",
    "NARODNE_NOVINE",
    "SUDREG",
];

// Short names for filters, where the facets endpoint returns only codes. Full names come from GET /api/sources.
export const SOURCE_SHORT_NAMES: Record<SourceCode, string> = {
    ASSOCIATIONS_OFFICE: "Ured za udruge",
    FZOEU: "FZOEU",
    EU_FUNDS_NPOO: "EU fondovi (NPOO)",
    EU_FUNDS_COHESION: "EU fondovi 2021.-2027.",
    HZZ: "HZZ",
    APPRRR: "APPRRR",
    MZOM: "MZOM",
    NARODNE_NOVINE: "Narodne novine",
    SUDREG: "Sudski registar",
};

export const SOURCE_METHOD_LABELS: Record<SourceMethod, string> = {
    API: "API",
    HTML: "Web stranica",
    PDF: "PDF dokumenti",
};

export const isSourceCode = (value: unknown): value is SourceCode => SOURCE_CODES.some((code) => code === value);
