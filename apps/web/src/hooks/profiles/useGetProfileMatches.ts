import {useQuery} from "@tanstack/react-query";
import {getProfileMatches} from "@/api";
import type {Profile} from "@/types";
import {profileKeys} from "./profileKeys.ts";

const FIVE_MINUTES = 5 * 60 * 1000;

export const useGetProfileMatches = (profile: Profile) =>
    useQuery({
        queryKey: profileKeys.matches(profile),
        queryFn: () => getProfileMatches(profile),
        staleTime: FIVE_MINUTES,
    });
