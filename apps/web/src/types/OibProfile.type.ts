import type {Profile} from "./Profile.type.ts";

// Mirrors OibProfileResponse from POST /api/profiles/from-oib (API-7).
export type OibProfile = {
    companyName: string;
    seat: {
        municipality: string | null;
        county: string | null;
    };
    activityCode: string | null;
    profile: Profile;
    source: "sudreg";
};
