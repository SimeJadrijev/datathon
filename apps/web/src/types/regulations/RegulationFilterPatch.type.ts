type RegulationFilterKey = "q" | "type" | "area" | "period";

// URL parameters a filter change sets; an empty string or undefined removes the parameter.
export type RegulationFilterPatch = Partial<Record<RegulationFilterKey, string>>;
