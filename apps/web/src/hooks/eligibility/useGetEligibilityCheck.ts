import {useMutation} from "@tanstack/react-query";
import {getEligibilityCheck} from "@/api";

export const useGetEligibilityCheck = () =>
    useMutation({
        mutationFn: getEligibilityCheck,
    });
