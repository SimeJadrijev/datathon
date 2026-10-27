import {classNames} from "@/utils";
import {Button} from "./Button.tsx";
import styles from "./StatePanel.module.css";

interface ErrorStateProps {
    title?: string;
    message?: string;
    onRetry?: () => void;
}

export const ErrorState = ({
    title = "Nešto nije u redu",
    message = "Podatke trenutno nije moguće učitati.",
    onRetry,
}: ErrorStateProps) => {
    return (
        <div className={styles.panel} role="alert">
            <span className={classNames(styles.icon, styles.iconDanger)} aria-hidden="true">
                <svg viewBox="0 0 24 24" fill="none">
                    <path
                        d="M12 8v5m0 3.5h.01M10.3 3.9 2.6 17.2A2 2 0 0 0 4.3 20h15.4a2 2 0 0 0 1.7-2.8L13.7 3.9a2 2 0 0 0-3.4 0Z"
                        stroke="currentColor"
                        strokeWidth="1.8"
                        strokeLinecap="round"
                        strokeLinejoin="round"
                    />
                </svg>
            </span>
            <h2 className={styles.title}>{title}</h2>
            <p className={styles.message}>{message}</p>
            {onRetry && (
                <div className={styles.actions}>
                    <Button onClick={onRetry}>Pokušaj ponovno</Button>
                </div>
            )}
        </div>
    );
};
