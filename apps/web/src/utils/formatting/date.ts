import {LOCALE} from "@/constants";

const MILLISECONDS_PER_DAY = 24 * 60 * 60 * 1000;
const DATE_ONLY_PATTERN = /^(\d{4})-(\d{2})-(\d{2})$/;

const dateFormatter = new Intl.DateTimeFormat(LOCALE, {
    day: "numeric",
    month: "numeric",
    year: "numeric",
});

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

export const isWithinLastDays = (value: string, days: number, today = new Date()) => {
    const daysUntil = getDaysUntil(value, today);
    return daysUntil <= 0 && daysUntil > -days;
};

const padTwoDigits = (value: number) => String(value).padStart(2, "0");

export const toIsoDate = (date: Date) =>
    `${date.getFullYear()}-${padTwoDigits(date.getMonth() + 1)}-${padTwoDigits(date.getDate())}`;

const monthFormatter = new Intl.DateTimeFormat(LOCALE, {month: "short"});

const timeFormatter = new Intl.DateTimeFormat(LOCALE, {hour: "2-digit", minute: "2-digit"});

export const getDateParts = (value: string) => {
    const date = parseDate(value);
    return {
        day: date.getDate(),
        month: monthFormatter.format(date).replace(".", ""),
        year: date.getFullYear(),
    };
};

export const formatDateTime = (value: string) => {
    const date = parseDate(value);
    return `${dateFormatter.format(date)} u ${timeFormatter.format(date)}`;
};
