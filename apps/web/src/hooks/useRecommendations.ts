import {useQuery} from "@tanstack/react-query";
import {matchProfile} from "@/api";
import type {Profile} from "@/types";
import {profileKeys} from "./profileKeys.ts";

const FIVE_MINUTES = 5 * 60 * 1000;

export const useRecommendations = (profile: Profile) =>
    useQuery({
        queryKey: profileKeys.match(profile),
        queryFn: () => matchProfile(profile),
        staleTime: FIVE_MINUTES,
    });
