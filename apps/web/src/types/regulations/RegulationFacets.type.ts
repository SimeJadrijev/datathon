import type {FacetCount} from "@/types/api";

// Mirrors RegulationFacets from GET /api/regulations/facets.
export type RegulationFacets = {
    type: FacetCount[];
    area: FacetCount[];
};
