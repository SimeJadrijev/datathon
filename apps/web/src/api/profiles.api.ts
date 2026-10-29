import {api} from "./base.ts";
import type {MatchResult, OibProfile, Profile} from "@/types";

export const matchProfile = (profile: Profile) =>
    api.post<MatchResult[], MatchResult[]>("/profiles/match", profile);

export const getProfileFromOib = (oib: string) =>
    api.post<OibProfile, OibProfile>("/profiles/from-oib", {oib});
