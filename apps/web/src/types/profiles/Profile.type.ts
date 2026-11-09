import type {Area, TargetGroup} from "@/types/codeLists";

export type Profile = {
    type: TargetGroup;
    additionalTypes?: TargetGroup[];
    areas: Area[];
    county?: string;
    keywords?: string[];
    activityCodes?: string[];
};
