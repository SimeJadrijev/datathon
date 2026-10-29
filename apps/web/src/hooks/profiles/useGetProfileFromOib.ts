import {useMutation} from "@tanstack/react-query";
import {getProfileFromOib} from "@/api";

// A mutation because the lookup runs on the user's submit, not on render.
export const useGetProfileFromOib = () =>
    useMutation({
        mutationFn: getProfileFromOib,
    });
