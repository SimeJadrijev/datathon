import {Link} from "wouter";
import {Icon, type IconName} from "@/components";
import {routes} from "@/routes";
import styles from "./BrowseLinks.module.css";

interface BrowseLink {
    path: string;
    icon: IconName;
    label: string;
    description: string;
}

const BROWSE_LINKS: BrowseLink[] = [
    {
        path: routes.FUNDING_CALLS,
        icon: "search",
        label: "Svi natječaji",
        description: "Natječaji, potpore i stipendije iz svih izvora",
    },
    {
        path: routes.REGULATIONS,
        icon: "document",
        label: "Zakoni i propisi",
        description: "Novi propisi iz Narodnih novina",
    },
    {
        path: routes.SOURCES,
        icon: "database",
        label: "Izvori podataka",
        description: "Odakle dolaze podaci i koliko su svježi",
    },
];

export const BrowseLinks = () => {
    return (
        <nav className={styles.panel} aria-labelledby="browse-title">
            <h2 id="browse-title" className={styles.title}>Pregledaj bez profila</h2>
            <ul className={styles.list}>
                {BROWSE_LINKS.map((browseLink) => (
                    <li key={browseLink.path}>
                        <Link href={browseLink.path} className={styles.link}>
                            <span className={styles.icon}>
                                <Icon name={browseLink.icon}/>
                            </span>
                            <span className={styles.text}>
                                <span className={styles.label}>{browseLink.label}</span>
                                <span className={styles.description}>{browseLink.description}</span>
                            </span>
                            <Icon name="arrowRight" size={14} className={styles.arrow}/>
                        </Link>
                    </li>
                ))}
            </ul>
        </nav>
    );
};
