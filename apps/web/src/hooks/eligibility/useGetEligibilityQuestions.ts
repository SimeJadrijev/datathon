import {useQuery} from "@tanstack/react-query";
import {getEligibilityQuestions} from "@/api";
import {eligibilityKeys} from "./eligibilityKeys.ts";

export const useGetEligibilityQuestions = (fundingCallId: number) =>
    useQuery({
        queryKey: eligibilityKeys.questions(fundingCallId),
        queryFn: () => getEligibilityQuestions(fundingCallId),
    });
