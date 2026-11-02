import {useQuery} from "@tanstack/react-query";
import {getRegulation} from "@/api";
import {regulationKeys} from "./regulationKeys.ts";

export const useGetRegulation = (id: number) =>
    useQuery({
        queryKey: regulationKeys.detail(id),
        queryFn: () => getRegulation(id),
    });
