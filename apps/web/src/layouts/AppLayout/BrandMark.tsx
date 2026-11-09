import styles from "./AppLayout.module.css";

export const BrandMark = () => {
    return (
        <span className={styles.brandMark} aria-hidden="true">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round">
                <path d="M5 19a10 10 0 0 1 14-14"/>
                <path d="M9 19a6 6 0 0 1 10-10"/>
                <circle cx="17" cy="17" r="2" fill="currentColor" stroke="none"/>
            </svg>
        </span>
    );
};
