import type {ReactNode} from "react";
import styles from "./ExternalLink.module.css";

interface ExternalLinkProps {
    href: string;
    children: ReactNode;
}

export const ExternalLink = ({href, children}: ExternalLinkProps) => {
    return (
        <a href={href} target="_blank" rel="noopener noreferrer" className={styles.link}>
            {children}
            <svg className={styles.icon} viewBox="0 0 16 16" fill="none" aria-hidden="true">
                <path
                    d="M6 3H3v10h10v-3M9 3h4v4M13 3 7 9"
                    stroke="currentColor"
                    strokeWidth="1.5"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                />
            </svg>
            <span className="visually-hidden">(otvara se u novoj kartici)</span>
        </a>
    );
};
