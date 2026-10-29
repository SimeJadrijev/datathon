export type SourceCode =
    | "ASSOCIATIONS_OFFICE"
    | "FZOEU"
    | "EU_FUNDS_NPOO"
    | "HZZ"
    | "APPRRR"
    | "MZOM"
    | "NARODNE_NOVINE"
    | "SUDREG";

export type SourceRef = {
    code: SourceCode;
    name: string;
};
