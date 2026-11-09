import {Link} from "wouter";
import {DeadlineDate, ExternalButtonLink, ExternalLink} from "@/components";
import {routes} from "@/routes";
import type {FundingCallDetail} from "@/types";
import {classNames, formatDate, formatDateTime, formatDeadline, getDaysUntil, isDeadlineUrgent} from "@/utils";
import {formatAmountRange, isAiField} from "./detailFormatting.ts";
import styles from "./FundingCallDetailPage.module.css";

const getRelativeDeadlineClassName = (deadline: string) => {
    if (getDaysUntil(deadline) < 0) {
        return styles.expired;
    }
    return isDeadlineUrgent(deadline) ? styles.urgent : undefined;
};

interface DeadlineSummaryProps {
    fundingCall: FundingCallDetail;
}

const DeadlineSummary = ({fundingCall}: DeadlineSummaryProps) => {
    const {deadline, deadlineNote} = fundingCall;
    const isAi = isAiField(fundingCall, "deadline");

    if (!deadline) {
        return (
            <div className={styles.deadlineRow}>
                <DeadlineDate deadline={null}/>
                <p className={styles.deadlineText}>{deadlineNote ?? "Rok nije objavljen, provjeri ga na izvoru."}</p>
            </div>
        );
    }
    return (
        <div className={styles.deadlineRow}>
            <DeadlineDate deadline={deadline} isExtractedByAi={isAi}/>
            <p className={styles.deadlineText}>
                <span className={classNames(styles.deadlineDate, "tabular-nums", isAi && styles.aiValue)}>
                    {formatDate(deadline)}
                </span>
                <span className={getRelativeDeadlineClassName(deadline)}>{formatDeadline(deadline)}</span>
            </p>
        </div>
    );
};

interface FundingCallAsideProps {
    fundingCall: FundingCallDetail;
}

export const FundingCallAside = ({fundingCall}: FundingCallAsideProps) => {
    const {source, fetchedAt, url} = fundingCall;
    const amountRange = formatAmountRange(fundingCall);

    return (
        <aside className={styles.aside} aria-label="Rok i izvor">
            <div className={styles.panel}>
                <p className={styles.panelLabel}>Rok prijave</p>
                <DeadlineSummary fundingCall={fundingCall}/>
                {amountRange && (
                    <p className={styles.amountLine}>
                        <span className={classNames("tabular-nums", isAiField(fundingCall, "minAmount", "maxAmount") && styles.aiValue)}>
                            {amountRange}
                        </span>{" "}
                        po prijavi
                    </p>
                )}
                {url && (
                    <ExternalButtonLink href={url} className={styles.primaryAction}>Otvori službenu objavu</ExternalButtonLink>
                )}
            </div>
            <section className={styles.panel} aria-labelledby="source-title">
                <h2 id="source-title" className={styles.panelTitle}>Izvor</h2>
                <p className={styles.sourceName}>{source.name}</p>
                <ExternalLink href={source.url} className={styles.sourceLink}>{source.url.replace(/^https?:\/\//, "")}</ExternalLink>
                <p className={styles.sourceMeta}>
                    {fetchedAt ? `Podaci dohvaćeni ${formatDateTime(fetchedAt)}.` : "Datum dohvata nije zabilježen."}
                    {" "}
                    <Link href={routes.SOURCES}>Svi izvori podataka</Link>
                </p>
            </section>
        </aside>
    );
};
