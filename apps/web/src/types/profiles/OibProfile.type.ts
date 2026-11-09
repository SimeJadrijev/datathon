import type {Profile} from "./Profile.type.ts";

export type OibProfile = {
    companyName: string;
    seat: {
        municipality: string | null;
        county: string | null;
    };
    activityCode: string | null;
    profile: Profile;
    source: "sudreg" | "registar-udruga";
};
