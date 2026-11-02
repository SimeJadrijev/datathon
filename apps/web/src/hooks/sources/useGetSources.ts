import {useQuery} from "@tanstack/react-query";
import {getSources} from "@/api";
import {sourceKeys} from "./sourceKeys.ts";

const FIVE_MINUTES = 5 * 60 * 1000;

export const useGetSources = () =>
    useQuery({
        queryKey: sourceKeys.all,
        queryFn: getSources,
        staleTime: FIVE_MINUTES,
    });
