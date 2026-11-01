import {classNames, getDateParts, getDaysUntil, isDeadlineUrgent} from "@/utils";
import styles from "./DeadlineDate.module.css";

interface DeadlineDateProps {
    deadline: string | null;
    isExtractedByAi?: boolean;
    isCompact?: boolean;
}

// Decorative: the row next to it says the deadline in words for screen readers.
export const DeadlineDate = ({deadline, isExtractedByAi = false, isCompact = false}: DeadlineDateProps) => {
    if (!deadline) {
        return (
            <span className={classNames(styles.date, styles.unknown, isCompact && styles.compact)} aria-hidden="true">
                <span className={styles.month}>rok</span>
                <span className={styles.day}>–</span>
            </span>
        );
    }

    const {day, month, year} = getDateParts(deadline);
    const isOtherYear = year !== new Date().getFullYear();

    return (
        <span
            className={classNames(
                styles.date,
                getDaysUntil(deadline) < 0 && styles.expired,
                isDeadlineUrgent(deadline) && styles.urgent,
                isExtractedByAi && styles.ai,
                isCompact && styles.compact,
            )}
            aria-hidden="true"
        >
            <span className={styles.month}>{month}</span>
            <span className={styles.day}>{day}</span>
            {isOtherYear && <span className={styles.year}>{year}</span>}
        </span>
    );
};
