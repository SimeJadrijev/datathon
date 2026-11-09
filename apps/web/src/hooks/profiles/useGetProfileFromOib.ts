import {useMutation} from "@tanstack/react-query";
import {getProfileFromOib} from "@/api";

export const useGetProfileFromOib = () =>
    useMutation({
        mutationFn: getProfileFromOib,
    });
