import {api} from "@/api/base.ts";
import type {Page, RegulationFilter, RegulationSummary} from "@/types";

export const getRegulations = (filter: RegulationFilter) =>
    api.get<Page<RegulationSummary>, Page<RegulationSummary>>("/regulations", {params: filter});
