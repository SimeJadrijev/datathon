import {useMutation} from "@tanstack/react-query";
import {getProfileFromOib} from "@/api";

export const useProfileFromOib = () =>
    useMutation({
        mutationFn: getProfileFromOib,
    });
