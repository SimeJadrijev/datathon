import {keepPreviousData, useQuery} from "@tanstack/react-query";
import {getRegulationFacets} from "@/api";
import type {RegulationFilter} from "@/types";
import {regulationKeys} from "./regulationKeys.ts";

export const useGetRegulationFacets = (filter: RegulationFilter) =>
    useQuery({
        queryKey: regulationKeys.facets(filter),
        queryFn: () => getRegulationFacets(filter),
        placeholderData: keepPreviousData,
    });
