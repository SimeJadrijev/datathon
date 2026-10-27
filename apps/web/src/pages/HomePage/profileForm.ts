import type {Area, Profile, TargetGroup} from "@/types";

const MAX_KEYWORDS = 10;

export interface ProfileFormValues {
    type: TargetGroup | "";
    areas: Area[];
    county: string;
    keywords: string;
}

export interface ProfileFormErrors {
    type?: string;
    areas?: string;
}

export const toFormValues = (profile?: Profile): ProfileFormValues => ({
    type: profile?.type ?? "",
    areas: profile?.areas ?? [],
    county: profile?.county ?? "",
    keywords: profile?.keywords?.join(", ") ?? "",
});

export const validateProfileForm = (values: ProfileFormValues): ProfileFormErrors => ({
    type: values.type === "" ? "Odaberi tko si." : undefined,
    areas: values.areas.length === 0 ? "Odaberi barem jedno područje koje te zanima." : undefined,
});

export const hasErrors = (errors: ProfileFormErrors) => Boolean(errors.type || errors.areas);

const parseKeywords = (keywords: string) => {
    const uniqueKeywords = new Set(
        keywords
            .split(",")
            .map((keyword) => keyword.trim())
            .filter(Boolean),
    );
    return [...uniqueKeywords].slice(0, MAX_KEYWORDS);
};

export const toProfile = (type: TargetGroup, values: ProfileFormValues): Profile => {
    const keywords = parseKeywords(values.keywords);

    return {
        type,
        areas: values.areas,
        county: values.county || undefined,
        keywords: keywords.length > 0 ? keywords : undefined,
    };
};
