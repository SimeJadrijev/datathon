import {Link} from "wouter";
import {Badge, Card} from "@/components";
import {FUNDING_CALL_STATUS_LABELS} from "@/constants";
import {fundingCallDetailPath} from "@/routes";
import type {CitingFundingCall} from "@/types";
import {formatDate} from "@/utils";
import styles from "./RegulationDetailPage.module.css";

const describe = ({provider, deadline}: CitingFundingCall) =>
    [provider, deadline && `rok ${formatDate(deadline)}`].filter(Boolean).join(" · ");

interface CitingFundingCallsProps {
    fundingCalls: CitingFundingCall[];
}

export const CitingFundingCalls = ({fundingCalls}: CitingFundingCallsProps) => (
    <Card as="section" aria-labelledby="citing-calls-title">
        <h2 id="citing-calls-title" className={styles.sectionTitle}>Natječaji koji se pozivaju na ovaj propis</h2>
        <ul className={styles.citingList}>
            {fundingCalls.map((fundingCall) => (
                <li key={fundingCall.id}>
                    <Link href={fundingCallDetailPath(fundingCall.id)}>{fundingCall.title}</Link>
                    <span className={styles.citingMeta}>
                        {fundingCall.status && fundingCall.status !== "OPEN" && (
                            <Badge tone="neutral">{FUNDING_CALL_STATUS_LABELS[fundingCall.status]}</Badge>
                        )}
                        {describe(fundingCall)}
                    </span>
                </li>
            ))}
        </ul>
    </Card>
);
