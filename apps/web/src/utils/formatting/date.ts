import {LOCALE} from "@/constants";

const MILLISECONDS_PER_DAY = 24 * 60 * 60 * 1000;
const DATE_ONLY_PATTERN = /^(\d{4})-(\d{2})-(\d{2})$/;

const dateFormatter = new Intl.DateTimeFormat(LOCALE, {
    day: "numeric",
    month: "numeric",
    year: "numeric",
});

// A date-only ISO string ("2026-10-23") is read as a local date, not as UTC midnight.
export const parseDate = (value: string) => {
    const match = DATE_ONLY_PATTERN.exec(value);
    if (!match) {
        return new Date(value);
    }

    const [, year, month, day] = match;
    return new Date(Number(year), Number(month) - 1, Number(day));
};

const startOfDay = (date: Date) => new Date(date.getFullYear(), date.getMonth(), date.getDate());

export const getDaysUntil = (value: string, today = new Date()) => {
    const difference = startOfDay(parseDate(value)).getTime() - startOfDay(today).getTime();
    return Math.round(difference / MILLISECONDS_PER_DAY);
};

export const formatDate = (value: string) => dateFormatter.format(parseDate(value));
