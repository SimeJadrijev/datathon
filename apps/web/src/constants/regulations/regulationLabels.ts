import type {RegulationPeriod, RegulationType} from "@/types";

export const REGULATION_TYPES: RegulationType[] = ["LAW", "DECREE", "ORDINANCE", "DECISION", "OTHER"];

export const REGULATION_TYPE_LABELS: Record<RegulationType, string> = {
    LAW: "Zakon",
    DECREE: "Uredba",
    ORDINANCE: "Pravilnik",
    DECISION: "Odluka",
    OTHER: "Ostalo",
};

// A regulation published within this many days is marked "Novo".
export const NEW_REGULATION_DAYS = 30;

export const REGULATION_PERIODS: RegulationPeriod[] = ["30d", "90d", "thisYear", "lastYear"];

export const REGULATION_PERIOD_LABELS: Record<RegulationPeriod, string> = {
    "30d": "Zadnjih 30 dana",
    "90d": "Zadnja 3 mjeseca",
    thisYear: "Ove godine",
    lastYear: "Prošle godine",
};

export const isRegulationType = (value: unknown): value is RegulationType =>
    REGULATION_TYPES.some((type) => type === value);

export const isRegulationPeriod = (value: unknown): value is RegulationPeriod =>
    REGULATION_PERIODS.some((period) => period === value);
