import type {Area} from "./Area.type.ts";
import type {TargetGroup} from "./TargetGroup.type.ts";

// Mirrors ProfileRequest from POST /api/profiles/match (API-6).
export type Profile = {
    type: TargetGroup;
    additionalTypes?: TargetGroup[];
    areas: Area[];
    county?: string;
    keywords?: string[];
    activityCodes?: string[];
};
