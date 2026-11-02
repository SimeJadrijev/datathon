import {useMutation} from "@tanstack/react-query";
import {getEligibilityCheck} from "@/api";

// A mutation because the check runs on the user's submit, not on render.
export const useGetEligibilityCheck = () =>
    useMutation({
        mutationFn: getEligibilityCheck,
    });
