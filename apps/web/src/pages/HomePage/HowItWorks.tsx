import {Link} from "wouter";
import {Card} from "@/components";
import {routes} from "@/routes";
import styles from "./HowItWorks.module.css";

export const HowItWorks = () => {
    return (
        <Card as="section" className={styles.card}>
            <h2 className={styles.title}>Kako radi</h2>
            <ol className={styles.steps}>
                <li>Odgovori na tri kratka pitanja.</li>
                <li>Pokazat ćemo ti otvorene natječaje koji ti odgovaraju i zašto.</li>
                <li>Svaki podatak ima poveznicu na službeni izvor.</li>
            </ol>
            <p className={styles.alternative}>
                Ne želiš profil? Pregledaj <Link href={routes.FUNDING_CALLS}>sve natječaje</Link> ili{" "}
                <Link href={routes.REGULATIONS}>nove zakone</Link>.
            </p>
        </Card>
    );
};
