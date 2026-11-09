type FundingCallFilterKey = "q" | "status" | "targetGroup" | "area" | "county" | "source" | "type" | "sort";

export type FundingCallFilterPatch = Partial<Record<FundingCallFilterKey, string>>;
