import type {FacetCount} from "@/types/api";

// Mirrors FundingCallFacets from GET /api/funding-calls/facets. Each facet ignores its own filter.
export type FundingCallFacets = {
    status: FacetCount[];
    targetGroup: FacetCount[];
    area: FacetCount[];
    county: FacetCount[];
    source: FacetCount[];
    type: FacetCount[];
};
