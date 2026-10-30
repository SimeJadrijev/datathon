import type {Area, TargetGroup} from "@/types/codeLists";

// Mirrors ProfileRequest from POST /api/profiles/match (API-6).
export type Profile = {
    type: TargetGroup;
    additionalTypes?: TargetGroup[];
    areas: Area[];
    county?: string;
    keywords?: string[];
    activityCodes?: string[];
};
