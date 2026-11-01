import {type ReactNode, useEffect} from "react";
import {Link, useLocation} from "wouter";
import {APP_NAME} from "@/constants";
import {routes} from "@/routes";
import {BrandMark} from "./BrandMark.tsx";
import {Navigation} from "./Navigation.tsx";
import styles from "./AppLayout.module.css";

const MAIN_CONTENT_ID = "main-content";

interface AppLayoutProps {
    children: ReactNode;
}

export const AppLayout = ({children}: AppLayoutProps) => {
    const [location] = useLocation();

    useEffect(() => {
        window.scrollTo(0, 0);
    }, [location]);

    return (
        <div className={styles.layout}>
            <a href={`#${MAIN_CONTENT_ID}`} className={styles.skipLink}>
                Preskoči na sadržaj
            </a>
            <header className={styles.header}>
                <div className={styles.headerInner}>
                    <Link href={routes.HOME} className={styles.brand}>
                        <BrandMark/>
                        <span translate="no">{APP_NAME}</span>
                    </Link>
                    <Navigation/>
                </div>
            </header>
            <main id={MAIN_CONTENT_ID} className={styles.main} tabIndex={-1}>
                {children}
            </main>
            <footer className={styles.footer}>
                <div className={styles.footerInner}>
                    <div className={styles.footerBrand}>
                        <p className={styles.footerName} translate="no">{APP_NAME}</p>
                        <p>Natječaji, potpore, stipendije i propisi iz službenih izvora na jednom mjestu.</p>
                    </div>
                    <ul className={styles.footerLinks}>
                        <li><Link href={routes.SOURCES}>Izvori podataka</Link></li>
                        <li>Datathon Portala otvorenih podataka 2026.</li>
                        <li>Informativno, nije pravni savjet.</li>
                    </ul>
                </div>
            </footer>
        </div>
    );
};
