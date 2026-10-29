import type {Area} from "@/types";

export const AREA_LABELS: Record<Area, string> = {
    ENTREPRENEURSHIP: "Poduzetništvo",
    EMPLOYMENT: "Zapošljavanje",
    EDUCATION: "Obrazovanje",
    SCIENCE: "Znanost",
    INNOVATION: "Inovacije",
    DIGITALIZATION: "Digitalizacija",
    ENERGY: "Energetika",
    ENVIRONMENT: "Okoliš",
    TRANSPORT: "Promet",
    AGRICULTURE: "Poljoprivreda",
    TOURISM: "Turizam",
    CULTURE: "Kultura",
    SPORT: "Sport",
    SOCIAL: "Socijalna skrb",
    HEALTH: "Zdravstvo",
    YOUTH: "Mladi",
    CIVIL_SOCIETY: "Civilno društvo",
};

export const AREAS: Area[] = [
    "ENTREPRENEURSHIP",
    "EMPLOYMENT",
    "EDUCATION",
    "SCIENCE",
    "INNOVATION",
    "DIGITALIZATION",
    "ENERGY",
    "ENVIRONMENT",
    "TRANSPORT",
    "AGRICULTURE",
    "TOURISM",
    "CULTURE",
    "SPORT",
    "SOCIAL",
    "HEALTH",
    "YOUTH",
    "CIVIL_SOCIETY",
];

export const isArea = (value: unknown): value is Area => AREAS.some((area) => area === value);
