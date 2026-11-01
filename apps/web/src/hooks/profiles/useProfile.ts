import {useState} from "react";
import {COUNTIES, isArea, isTargetGroup} from "@/constants";
import type {Profile} from "@/types";

const STORAGE_KEY = "datathon.profile.v1";

const isStringArray = (value: unknown): value is string[] =>
    Array.isArray(value) && value.every((item) => typeof item === "string");

// Profiles saved before keyword validation may hold keywords the API rejects (3 to 50 characters).
const parseKeywords = (value: unknown) => {
    if (!isStringArray(value)) {
        return undefined;
    }
    const keywords = value.filter((keyword) => keyword.length >= 3 && keyword.length <= 50);
    return keywords.length > 0 ? keywords : undefined;
};

const parseProfile = (value: unknown): Profile | undefined => {
    if (typeof value !== "object" || value === null) {
        return undefined;
    }

    const {type, additionalTypes, areas, county, keywords, activityCodes} = value as Record<string, unknown>;
    const hasValidAreas = Array.isArray(areas) && areas.length > 0 && areas.every(isArea);
    if (!isTargetGroup(type) || !hasValidAreas) {
        return undefined;
    }

    return {
        type,
        areas,
        additionalTypes: Array.isArray(additionalTypes) ? additionalTypes.filter(isTargetGroup) : undefined,
        county: typeof county === "string" && COUNTIES.includes(county) ? county : undefined,
        keywords: parseKeywords(keywords),
        activityCodes: isStringArray(activityCodes) ? activityCodes : undefined,
    };
};

const readStoredProfile = () => {
    try {
        const storedValue = localStorage.getItem(STORAGE_KEY);
        return storedValue ? parseProfile(JSON.parse(storedValue)) : undefined;
    } catch {
        return undefined;
    }
};

const writeStoredProfile = (profile: Profile | undefined) => {
    try {
        if (profile) {
            localStorage.setItem(STORAGE_KEY, JSON.stringify(profile));
        } else {
            localStorage.removeItem(STORAGE_KEY);
        }
    } catch {
        // Storage can be blocked (private mode); the profile then lives only in memory.
    }
};

export const useProfile = () => {
    const [profile, setProfile] = useState(readStoredProfile);

    const saveProfile = (nextProfile: Profile) => {
        writeStoredProfile(nextProfile);
        setProfile(nextProfile);
    };

    const clearProfile = () => {
        writeStoredProfile(undefined);
        setProfile(undefined);
    };

    return {profile, saveProfile, clearProfile};
};
