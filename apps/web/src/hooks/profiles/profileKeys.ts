import type {Profile} from "@/types";

export const profileKeys = {
    all: ["profiles"] as const,
    matches: (profile: Profile) => [...profileKeys.all, "matches", profile] as const,
};
