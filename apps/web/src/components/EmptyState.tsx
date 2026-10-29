import type {ReactNode} from "react";
import styles from "./StatePanel.module.css";

interface EmptyStateProps {
    title: string;
    hint?: string;
    actions?: ReactNode;
}

export const EmptyState = ({title, hint, actions}: EmptyStateProps) => {
    return (
        <div className={styles.panel}>
            <span className={styles.icon} aria-hidden="true">
                <svg viewBox="0 0 24 24" fill="none">
                    <path
                        d="m20 20-4.2-4.2M17 10.5a6.5 6.5 0 1 1-13 0 6.5 6.5 0 0 1 13 0Z"
                        stroke="currentColor"
                        strokeWidth="1.8"
                        strokeLinecap="round"
                    />
                </svg>
            </span>
            <h2 className={styles.title}>{title}</h2>
            {hint && <p className={styles.message}>{hint}</p>}
            {actions && <div className={styles.actions}>{actions}</div>}
        </div>
    );
};
