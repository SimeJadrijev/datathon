import {useQuery} from "@tanstack/react-query";
import {getMeta} from "@/api";
import {metaKeys} from "./metaKeys.ts";

const FIVE_MINUTES = 5 * 60 * 1000;

export const useGetMeta = () =>
    useQuery({
        queryKey: metaKeys.all,
        queryFn: getMeta,
        staleTime: FIVE_MINUTES,
    });
