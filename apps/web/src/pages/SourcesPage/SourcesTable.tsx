import {ExternalLink} from "@/components";
import {SOURCE_METHOD_LABELS} from "@/constants";
import type {SourceSummary} from "@/types";
import {classNames, formatCount, formatDate} from "@/utils";
import styles from "./SourcesPage.module.css";

const UNVERIFIED_LICENSE = "nije provjereno";

const formatHost = (url: string) => url.replace(/^https?:\/\//, "").replace(/\/$/, "");

// The Court Register is not stored: it is asked live each time someone looks up an OIB.
const describeLastFetch = ({code, lastSuccessfulFetch}: SourceSummary) => {
    if (lastSuccessfulFetch) {
        return formatDate(lastSuccessfulFetch);
    }
    return code === "SUDREG" ? "uživo, za svaki OIB" : "još nije dohvaćen";
};

interface SourcesTableProps {
    sources: SourceSummary[];
}

// A real table on wide screens; under 768 px every row becomes a small card with labelled cells (data-label).
export const SourcesTable = ({sources}: SourcesTableProps) => {
    return (
        <div className={styles.tableFrame}>
            <table className={styles.table}>
                <thead>
                    <tr>
                        <th scope="col">Izvor</th>
                        <th scope="col">Što pokriva</th>
                        <th scope="col">Način</th>
                        <th scope="col" className={styles.numeric}>Zapisa</th>
                        <th scope="col">Zadnji dohvat</th>
                        <th scope="col">Licenca</th>
                    </tr>
                </thead>
                <tbody>
                    {sources.map((source) => (
                        <tr key={source.code}>
                            <th scope="row" className={styles.sourceCell}>
                                <span className={styles.sourceName}>{source.name}</span>
                                <ExternalLink href={source.url} className={styles.sourceLink}>{formatHost(source.url)}</ExternalLink>
                            </th>
                            <td data-label="Što pokriva" className={styles.description}>{source.description}</td>
                            <td data-label="Način" className={styles.noWrap}>{SOURCE_METHOD_LABELS[source.method]}</td>
                            <td data-label="Zapisa" className={classNames(styles.numeric, "tabular-nums")}>
                                {source.recordCount !== null ? formatCount(source.recordCount) : "–"}
                            </td>
                            <td data-label="Zadnji dohvat" className={classNames(styles.noWrap, "tabular-nums")}>
                                {describeLastFetch(source)}
                            </td>
                            <td data-label="Licenca" className={source.license === UNVERIFIED_LICENSE ? styles.muted : undefined}>
                                {source.license}
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
};
