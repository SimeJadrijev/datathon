import type {Area} from "@/types/codeLists";
import type {RegulationType} from "./Regulation.type.ts";

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
