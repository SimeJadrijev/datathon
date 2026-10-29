import type {ReactNode} from "react";
import {Link} from "wouter";
import {FUNDING_CALL_STATUS_LABELS, FUNDING_CALL_TYPE_LABELS} from "@/constants";
import {fundingCallDetailPath} from "@/routes";
import type {FundingCallSummary} from "@/types";
import {classNames, formatAmount, formatDate, formatDeadline, getDaysUntil, isDeadlineUrgent} from "@/utils";
import {Badge} from "@/components/Badge";
import {DeadlineDate} from "@/components/DeadlineDate";
import {ExternalLink} from "@/components/ExternalLink";
import styles from "./FundingCallRow.module.css";

const formatAmounts = ({maxAmount, totalAmount}: Pick<FundingCallSummary, "maxAmount" | "totalAmount">) => {
    if (maxAmount !== null) {
        return {value: `do ${formatAmount(maxAmount)}`, label: "po prijavi"};
    }
    if (totalAmount !== null) {
        return {value: formatAmount(totalAmount), label: "ukupno"};
    }
    return undefined;
};

const getDeadlineClassName = (deadline: string) => {
    if (getDaysUntil(deadline) < 0) {
        return styles.expired;
    }
    return isDeadlineUrgent(deadline) ? styles.urgent : undefined;
};

const DeadlineText = ({deadline, deadlineNote}: Pick<FundingCallSummary, "deadline" | "deadlineNote">) => {
    if (deadline) {
        return (
            <p className={classNames(styles.deadline, getDeadlineClassName(deadline))}>
                <span className="visually-hidden">Rok {formatDate(deadline)}, </span>
                {formatDeadline(deadline)}
            </p>
        );
    }
    return <p className={styles.deadline}>{deadlineNote ?? "Rok: provjeri na izvoru"}</p>;
};

interface FundingCallRowProps {
    fundingCall: FundingCallSummary;
    children?: ReactNode;
}

// One funding call in a list. Goes inside FundingCallList, which draws the frame and the dividers.
export const FundingCallRow = ({fundingCall, children}: FundingCallRowProps) => {
    const amount = formatAmounts(fundingCall);
    const isAi = fundingCall.extractedByLlm;
    const typeLabel = fundingCall.type ? FUNDING_CALL_TYPE_LABELS[fundingCall.type] : undefined;
    const metaText = [typeLabel, fundingCall.provider].filter(Boolean).join(" · ");
    const isNotOpen = fundingCall.status !== null && fundingCall.status !== "OPEN";

    return (
        <article className={styles.row}>
            <div className={styles.date}>
                <DeadlineDate deadline={fundingCall.deadline} isExtractedByAi={isAi}/>
            </div>
            <div className={styles.main}>
                <p className={styles.meta}>
                    {isNotOpen && fundingCall.status && (
                        <Badge tone="info">{FUNDING_CALL_STATUS_LABELS[fundingCall.status]}</Badge>
                    )}
                    <span className={styles.metaText}>{metaText}</span>
                </p>
                <h3 className={styles.title}>
                    <Link href={fundingCallDetailPath(fundingCall.id)} className={styles.titleLink}>
                        {fundingCall.title}
                    </Link>
                </h3>
                {children}
                <p className={styles.source}>
                    Izvor: {fundingCall.source.name}
                    {isAi && (
                        <span className={styles.aiNote}>
                            {" · "}rok i iznos pročitao je AI iz teksta objave, provjeri ih na izvoru
                        </span>
                    )}
                </p>
            </div>
            <div className={styles.side}>
                {amount && (
                    <p className={styles.amount}>
                        <span className={classNames("tabular-nums", isAi && styles.aiValue)}>{amount.value}</span>
                        <span className={styles.amountLabel}>{amount.label}</span>
                    </p>
                )}
                <DeadlineText deadline={fundingCall.deadline} deadlineNote={fundingCall.deadlineNote}/>
                <ExternalLink href={fundingCall.url} className={styles.sourceLink}>Službena objava</ExternalLink>
            </div>
        </article>
    );
};
