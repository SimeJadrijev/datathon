import type {TargetGroup} from "@/types";

export const TARGET_GROUPS: TargetGroup[] = [
    "CITIZEN",
    "STUDENT",
    "CRAFT_BUSINESS",
    "COMPANY",
    "ASSOCIATION",
    "FARMER",
    "LOCAL_GOVERNMENT",
];

export const TARGET_GROUP_LABELS: Record<TargetGroup, string> = {
    CITIZEN: "Građanin",
    STUDENT: "Student",
    CRAFT_BUSINESS: "Obrt",
    COMPANY: "Tvrtka",
    ASSOCIATION: "Udruga",
    FARMER: "Poljoprivrednik",
    LOCAL_GOVERNMENT: "Jedinica lokalne samouprave",
};

export const TARGET_GROUP_DESCRIPTIONS: Record<TargetGroup, string> = {
    CITIZEN: "fizička osoba, kućanstvo",
    STUDENT: "učenik ili student",
    CRAFT_BUSINESS: "obrt, samozaposleni",
    COMPANY: "d.o.o., j.d.o.o., d.d.",
    ASSOCIATION: "udruga, zaklada, OCD",
    FARMER: "OPG, poljoprivredno gospodarstvo",
    LOCAL_GOVERNMENT: "općina, grad, županija",
};

export const isTargetGroup = (value: unknown): value is TargetGroup =>
    TARGET_GROUPS.some((targetGroup) => targetGroup === value);
