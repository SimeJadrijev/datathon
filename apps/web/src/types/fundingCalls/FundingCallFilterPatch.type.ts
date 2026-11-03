type FundingCallFilterKey = "q" | "status" | "targetGroup" | "area" | "county" | "source" | "type" | "sort";

// URL parameters a filter change sets; an empty string or undefined removes the parameter.
export type FundingCallFilterPatch = Partial<Record<FundingCallFilterKey, string>>;
