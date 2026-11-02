import {api} from "@/api/base.ts";
import type {RegulationFacets, RegulationFilter} from "@/types";

export const getRegulationFacets = (filter: RegulationFilter) =>
    api.get<RegulationFacets, RegulationFacets>("/regulations/facets", {params: filter});
