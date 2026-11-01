import {api} from "@/api/base.ts";
import type {MatchResult, Profile} from "@/types";

export const getProfileMatches = (profile: Profile) =>
    api.post<MatchResult[], MatchResult[]>("/profiles/match", profile);
