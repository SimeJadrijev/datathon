import type {ReactNode} from "react";
import {Link} from "wouter";
import {FUNDING_CALL_STATUS_LABELS, FUNDING_CALL_TYPE_LABELS} from "@/constants";
import {fundingCallDetailPath} from "@/routes";
import type {FundingCallStatus, FundingCallSummary} from "@/types";
import {formatAmount, formatDate, formatDeadline, getDaysUntil, isDeadlineUrgent} from "@/utils";
import {Badge, type BadgeTone} from "./Badge.tsx";
import {Card} from "./Card.tsx";
import {ExternalLink} from "./ExternalLink.tsx";
import styles from "./FundingCallCard.module.css";

const STATUS_TONES: Record<FundingCallStatus, BadgeTone> = {
    OPEN: "success",
    ANNOUNCED: "accent",
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
                {formatDate(deadline)} <Badge tone={getDeadlineTone(deadline)}>{formatDeadline(deadline)}</Badge>
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
        <Card as="article" className={styles.card}>
            <div className={styles.badges}>
                {fundingCall.status && (
                    <Badge tone={STATUS_TONES[fundingCall.status]}>{FUNDING_CALL_STATUS_LABELS[fundingCall.status]}</Badge>
                )}
                {fundingCall.type && <Badge>{FUNDING_CALL_TYPE_LABELS[fundingCall.type]}</Badge>}
                {fundingCall.extractedByLlm && (
                    <Badge tone="ai" title="Dio podataka (rok, iznosi, skupine) izvučen je iz teksta pomoću AI-ja">
                        AI
                    </Badge>
                )}
            </div>
            <h3 className={styles.title}>
                <Link href={fundingCallDetailPath(fundingCall.id)} className={styles.titleLink} title={fundingCall.title}>
                    {fundingCall.title}
                </Link>
            </h3>
            {fundingCall.provider && <p className={styles.provider}>{fundingCall.provider}</p>}
            <dl className={styles.facts}>
                <div>
                    <dt>Rok</dt>
                    <dd><DeadlineValue deadline={fundingCall.deadline} deadlineNote={fundingCall.deadlineNote}/></dd>
                </div>
                {amount && (
                    <div>
                        <dt>Iznos</dt>
                        <dd>{amount}</dd>
                    </div>
                )}
            </dl>
            {children}
            <footer className={styles.footer}>
                <span>Izvor: {fundingCall.source.name}</span>
                <span className={styles.sourceLink}>
                    <ExternalLink href={fundingCall.url}>Otvori na izvoru</ExternalLink>
                </span>
            </footer>
        </Card>
    );
};
