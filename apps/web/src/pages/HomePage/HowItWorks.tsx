import {Link} from "wouter";
import {routes} from "@/routes";
import styles from "./SidePanel.module.css";

const BROWSE_LINKS = [
    {path: routes.FUNDING_CALLS, label: "Svi natječaji", description: "natječaji, potpore i stipendije iz svih izvora"},
    {path: routes.REGULATIONS, label: "Zakoni i propisi", description: "novi propisi iz Narodnih novina"},
    {path: routes.SOURCES, label: "Izvori podataka", description: "odakle dolaze podaci i koliko su svježi"},
];

// Shown before a profile exists: what happens next, and a way in without a profile.
export const HowItWorks = () => {
    return (
        <section className={styles.panel} aria-labelledby="how-it-works-title">
            <h2 id="how-it-works-title" className={styles.heading}>Kako radi</h2>
            <ol className={styles.steps}>
                <li>
                    <strong>Odgovori na tri kratka pitanja</strong>
                    <span>ili upiši OIB tvrtke ili udruge pa obrazac popunimo iz Sudskog registra ili Registra udruga.</span>
                </li>
                <li>
                    <strong>Pogledaj natječaje koji ti odgovaraju</strong>
                    <span>poredane po tome koliko ti odgovaraju, s objašnjenjem zašto.</span>
                </li>
                <li>
                    <strong>Provjeri na službenom izvoru</strong>
                    <span>svaki natječaj ima poveznicu na izvornu objavu.</span>
                </li>
            </ol>
            <h3 className={styles.subheading}>Ili pregledaj bez profila</h3>
            <ul className={styles.links}>
                {BROWSE_LINKS.map((browseLink) => (
                    <li key={browseLink.path}>
                        <Link href={browseLink.path} className={styles.link}>{browseLink.label}</Link>
                        <span className={styles.detail}>{browseLink.description}</span>
                    </li>
                ))}
            </ul>
        </section>
    );
};
