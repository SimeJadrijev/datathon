import type {FallbackProps} from "react-error-boundary";
import {ErrorState} from "@/components";
import styles from "./ErrorPage.module.css";

export const ErrorPage = ({resetErrorBoundary}: FallbackProps) => {
    return (
        <main className={styles.page}>
            <ErrorState
                title="Nešto je pošlo po zlu"
                message="Došlo je do neočekivane greške. Pokušaj ponovno ili osvježi stranicu."
                onRetry={resetErrorBoundary}
            />
        </main>
    );
};
