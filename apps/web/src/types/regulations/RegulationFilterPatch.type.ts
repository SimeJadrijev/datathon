type RegulationFilterKey = "q" | "type" | "area" | "period";

export type RegulationFilterPatch = Partial<Record<RegulationFilterKey, string>>;
