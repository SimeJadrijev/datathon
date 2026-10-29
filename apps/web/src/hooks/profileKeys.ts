import type {Profile} from "@/types";

export const profileKeys = {
    all: ["profiles"] as const,
    match: (profile: Profile) => [...profileKeys.all, "match", profile] as const,
};
