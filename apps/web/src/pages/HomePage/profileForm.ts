import {findCounty} from "@/constants";
import type {Area, OibProfile, Profile, TargetGroup} from "@/types";

const MAX_KEYWORDS = 10;
const MIN_KEYWORD_LENGTH = 3;
const MAX_KEYWORD_LENGTH = 50;

export interface ProfileFormValues {
    type: TargetGroup | "";
    areas: Area[];
    county: string;
    keywords: string;
    activityCodes: string[];
}

export interface ProfileFormErrors {
    type?: string;
    areas?: string;
    keywords?: string;
}

export const toFormValues = (profile?: Profile): ProfileFormValues => ({
    type: profile?.type ?? "",
    areas: profile?.areas ?? [],
    county: profile?.county ?? "",
    keywords: profile?.keywords?.join(", ") ?? "",
    activityCodes: profile?.activityCodes ?? [],
});

export const fromOibProfile = (oibProfile: OibProfile, values: ProfileFormValues): ProfileFormValues => ({
    ...values,
    type: oibProfile.profile.type,
    areas: oibProfile.profile.areas,
    county: findCounty(oibProfile.profile.county) ?? values.county,
    activityCodes: oibProfile.profile.activityCodes ?? [],
});

const parseKeywords = (keywords: string) => {
    const uniqueKeywords = new Set(
        keywords
            .split(",")
            .map((keyword) => keyword.trim())
            .filter(Boolean),
    );
    return [...uniqueKeywords].slice(0, MAX_KEYWORDS);
};

const validateKeywords = (keywords: string) => {
    const invalidKeyword = parseKeywords(keywords).find(
        (keyword) => keyword.length < MIN_KEYWORD_LENGTH || keyword.length > MAX_KEYWORD_LENGTH,
    );
    if (!invalidKeyword) {
        return undefined;
    }
    return `Ključna riječ "${invalidKeyword}" mora imati od ${MIN_KEYWORD_LENGTH} do ${MAX_KEYWORD_LENGTH} znakova.`;
};

export const validateProfileForm = (values: ProfileFormValues): ProfileFormErrors => ({
    type: values.type === "" ? "Odaberi tko si." : undefined,
    areas: values.areas.length === 0 ? "Odaberi barem jedno područje koje te zanima." : undefined,
    keywords: validateKeywords(values.keywords),
});

export const hasErrors = (errors: ProfileFormErrors) => Boolean(errors.type || errors.areas || errors.keywords);

export const toProfile = (type: TargetGroup, values: ProfileFormValues): Profile => {
    const keywords = parseKeywords(values.keywords);

    return {
        type,
        areas: values.areas,
        county: values.county || undefined,
        keywords: keywords.length > 0 ? keywords : undefined,
        activityCodes: values.activityCodes.length > 0 ? values.activityCodes : undefined,
    };
};
