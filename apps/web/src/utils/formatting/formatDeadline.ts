import {getDaysUntil} from "./date.ts";

export const URGENT_DEADLINE_DAYS = 7;

const pluralizeDays = (days: number) => {
    const isSingular = days % 10 === 1 && days % 100 !== 11;
    return isSingular ? `${days} dan` : `${days} dana`;
};

export const formatDeadline = (deadline: string, today = new Date()) => {
    const days = getDaysUntil(deadline, today);

    if (days < 0) {
        return "isteklo";
    }
    if (days === 0) {
        return "ističe danas";
    }
    if (days === 1) {
        return "ističe sutra";
    }
    return `još ${pluralizeDays(days)}`;
};

export const isDeadlineUrgent = (deadline: string, today = new Date()) => {
    const days = getDaysUntil(deadline, today);
    return days >= 0 && days <= URGENT_DEADLINE_DAYS;
};
