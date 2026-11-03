import {keepPreviousData, useQuery} from "@tanstack/react-query";
import {getRegulations} from "@/api";
import type {RegulationFilter} from "@/types";
import {regulationKeys} from "./regulationKeys.ts";

export const useGetRegulations = (filter: RegulationFilter) =>
    useQuery({
        queryKey: regulationKeys.list(filter),
        queryFn: () => getRegulations(filter),
        placeholderData: keepPreviousData,
    });
