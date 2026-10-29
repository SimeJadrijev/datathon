import styles from "./ListSkeleton.module.css";

const DEFAULT_ROWS = 4;

interface ListSkeletonProps {
    label: string;
    rows?: number;
}

// Loading state for lists: keeps the layout stable while data arrives. The label is read by screen readers.
export const ListSkeleton = ({label, rows = DEFAULT_ROWS}: ListSkeletonProps) => {
    const rowIds = Array.from({length: rows}, (_, index) => `skeleton-${index}`);

    return (
        <div role="status" aria-live="polite">
            <p className={styles.label}>{label}</p>
            <ul className={styles.list} aria-hidden="true">
                {rowIds.map((rowId) => (
                    <li key={rowId} className={styles.row}>
                        <span className={styles.lineShort}/>
                        <span className={styles.lineTitle}/>
                        <span className={styles.lineMedium}/>
                    </li>
                ))}
            </ul>
        </div>
    );
};
