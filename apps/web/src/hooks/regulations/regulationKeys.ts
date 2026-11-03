import type {RegulationFilter} from "@/types";

export const regulationKeys = {
    all: ["regulations"] as const,
    list: (filter: RegulationFilter) => [...regulationKeys.all, "list", filter] as const,
    facets: (filter: RegulationFilter) => [...regulationKeys.all, "facets", filter] as const,
    detail: (id: number) => [...regulationKeys.all, "detail", id] as const,
};
