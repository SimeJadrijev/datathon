import type {ReactNode} from "react";
import {Link} from "wouter";
import {FUNDING_CALL_STATUS_LABELS, FUNDING_CALL_TYPE_LABELS} from "@/constants";
import {fundingCallDetailPath} from "@/routes";
import type {FundingCallStatus, FundingCallSummary} from "@/types";
import {formatAmount, formatDate, formatDeadline, getDaysUntil, isDeadlineUrgent} from "@/utils";
import {Badge, type BadgeTone} from "./Badge.tsx";
import {ExternalLink} from "./ExternalLink.tsx";
import {Icon} from "./Icon.tsx";
import styles from "./FundingCallCard.module.css";

const STATUS_TONES: Record<FundingCallStatus, BadgeTone> = {
    OPEN: "success",
    ANNOUNCED: "info",
    CLOSED: "neutral",
};

const getDeadlineTone = (deadline: string): BadgeTone => {
    if (getDaysUntil(deadline) < 0) {
        return "danger";
    }
    return isDeadlineUrgent(deadline) ? "warning" : "neutral";
};

const DeadlineValue = ({deadline, deadlineNote}: Pick<FundingCallSummary, "deadline" | "deadlineNote">) => {
    if (deadline) {
        return (
            <>
                <span className="tabular-nums">{formatDate(deadline)}</span>
                <Badge tone={getDeadlineTone(deadline)}>{formatDeadline(deadline)}</Badge>
            </>
        );
    }
    if (deadlineNote) {
        return <>{deadlineNote}</>;
    }
    return <span className={styles.muted}>provjeri na izvoru</span>;
};

const formatAmounts = ({maxAmount, totalAmount}: Pick<FundingCallSummary, "maxAmount" | "totalAmount">) => {
    if (maxAmount !== null) {
        return `do ${formatAmount(maxAmount)}`;
    }
    if (totalAmount !== null) {
        return `${formatAmount(totalAmount)} ukupno`;
    }
    return undefined;
};

interface FundingCallCardProps {
    fundingCall: FundingCallSummary;
    children?: ReactNode;
}

export const FundingCallCard = ({fundingCall, children}: FundingCallCardProps) => {
    const amount = formatAmounts(fundingCall);

    return (
        <article className={styles.card}>
            <div className={styles.main}>
                <div className={styles.badges}>
                    {fundingCall.status && (
                        <Badge tone={STATUS_TONES[fundingCall.status]}>
                            {FUNDING_CALL_STATUS_LABELS[fundingCall.status]}
                        </Badge>
                    )}
                    {fundingCall.type && <Badge>{FUNDING_CALL_TYPE_LABELS[fundingCall.type]}</Badge>}
                    {fundingCall.extractedByLlm && (
                        <Badge
                            tone="ai"
                            icon="sparkles"
                            title="Dio podataka (rok, iznosi, skupine) izvučen je iz teksta pomoću AI-ja. Provjeri na izvoru."
                        >
                            AI podaci
                        </Badge>
                    )}
                </div>
                <h3 className={styles.title}>
                    <Link href={fundingCallDetailPath(fundingCall.id)} className={styles.titleLink} title={fundingCall.title}>
                        {fundingCall.title}
                    </Link>
                </h3>
                {fundingCall.provider && (
                    <p className={styles.provider}>
                        <Icon name="institution" size={14} className={styles.providerIcon}/>
                        {fundingCall.provider}
                    </p>
                )}
            </div>
            <dl className={styles.facts}>
                <div className={styles.fact}>
                    <dt>Rok</dt>
                    <dd><DeadlineValue deadline={fundingCall.deadline} deadlineNote={fundingCall.deadlineNote}/></dd>
                </div>
                {amount && (
                    <div className={styles.fact}>
                        <dt>Iznos</dt>
                        <dd className="tabular-nums">{amount}</dd>
                    </div>
                )}
            </dl>
            {children && <div className={styles.extra}>{children}</div>}
            <footer className={styles.footer}>
                <span className={styles.source}>
                    <Icon name="database" size={13}/>
                    Izvor: {fundingCall.source.name}
                </span>
                <ExternalLink href={fundingCall.url} className={styles.sourceLink}>Službena objava</ExternalLink>
            </footer>
        </article>
    );
};
