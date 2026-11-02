import type {FacetCount} from "@/types/api";
import type {Area} from "@/types/codeLists";
import type {RegulationType} from "./Regulation.type.ts";

// Publish period the user picks in the list; the page turns it into publishedFrom and publishedTo.
export type RegulationPeriod = "30d" | "90d" | "thisYear" | "lastYear";

// Query parameters of GET /api/regulations and /facets. Dates are ISO (2026-01-01).
export type RegulationFilter = {
    q?: string;
    type?: RegulationType;
    area?: Area;
    publishedFrom?: string;
    publishedTo?: string;
    page?: number;
    size?: number;
};

// Mirrors RegulationFacets from GET /api/regulations/facets.
export type RegulationFacets = {
    type: FacetCount[];
    area: FacetCount[];
};
