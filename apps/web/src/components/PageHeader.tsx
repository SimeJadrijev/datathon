import type {ReactNode} from "react";
import styles from "./PageHeader.module.css";

interface PageHeaderProps {
    title: string;
    eyebrow?: string;
    description?: string;
    actions?: ReactNode;
}

export const PageHeader = ({title, eyebrow, description, actions}: PageHeaderProps) => {
    return (
        <header className={styles.header}>
            <div className={styles.text}>
                {eyebrow && <p className={styles.eyebrow}>{eyebrow}</p>}
                <h1 className={styles.title}>{title}</h1>
                {description && <p className={styles.description}>{description}</p>}
            </div>
            {actions && <div className={styles.actions}>{actions}</div>}
        </header>
    );
};
