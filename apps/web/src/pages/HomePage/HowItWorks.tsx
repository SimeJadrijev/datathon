import {Card} from "@/components";
import styles from "./HowItWorks.module.css";

export const HowItWorks = () => {
    return (
        <Card as="section" className={styles.card}>
            <h2 className={styles.title}>Kako radi</h2>
            <ol className={styles.steps}>
                <li>
                    <strong>Odgovori na tri kratka pitanja</strong>
                    <span>ili upiši OIB tvrtke pa obrazac popunimo iz Sudskog registra.</span>
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
        </Card>
    );
};
