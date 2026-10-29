import {type ReactNode, useEffect} from "react";
import {Link, useLocation} from "wouter";
import {APP_NAME} from "@/constants";
import {routes} from "@/routes";
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
                        <span className={styles.brandMark} aria-hidden="true">NZ</span>
                        {APP_NAME}
                    </Link>
                    <Navigation/>
                </div>
            </header>
            <main id={MAIN_CONTENT_ID} className={styles.main} tabIndex={-1}>
                {children}
            </main>
            <footer className={styles.footer}>
                <div className={styles.footerInner}>
                    <p>{APP_NAME} · Datathon Portala otvorenih podataka 2026.</p>
                    <p>Informativno, nije pravni savjet.</p>
                </div>
            </footer>
        </div>
    );
};
