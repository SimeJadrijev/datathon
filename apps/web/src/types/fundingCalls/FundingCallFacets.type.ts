import type {FacetCount} from "@/types/api";

export type FundingCallFacets = {
    status: FacetCount[];
    targetGroup: FacetCount[];
    area: FacetCount[];
    county: FacetCount[];
    source: FacetCount[];
    type: FacetCount[];
};
