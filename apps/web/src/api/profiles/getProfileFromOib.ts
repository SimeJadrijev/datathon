import {api} from "@/api/base.ts";
import type {OibProfile} from "@/types";

export const getProfileFromOib = (oib: string) =>
    api.post<OibProfile, OibProfile>("/profiles/from-oib", {oib});
