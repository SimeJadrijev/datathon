import {Badge, type BadgeTone, PageHeader} from "@/components";
import {FUNDING_CALL_STATUS_LABELS, FUNDING_CALL_TYPE_LABELS} from "@/constants";
import type {FundingCallDetail as FundingCallDetailType, FundingCallStatus} from "@/types";
import {formatDate} from "@/utils";
import {EligibilityPanel} from "./EligibilityPanel.tsx";
import {FundingCallAside} from "./FundingCallAside.tsx";
import {FundingCallFacts} from "./FundingCallFacts.tsx";
import {FundingCallSections} from "./FundingCallSections.tsx";
import {FundingCallStatusNotice} from "./FundingCallStatusNotice.tsx";
import styles from "./FundingCallDetailPage.module.css";

const STATUS_TONES: Record<FundingCallStatus, BadgeTone> = {
    OPEN: "success",
    ANNOUNCED: "info",
    CLOSED: "neutral",
};

interface FundingCallDetailProps {
    fundingCall: FundingCallDetailType;
}

export const FundingCallDetail = ({fundingCall}: FundingCallDetailProps) => {
    const {status, type, provider, publishedAt} = fundingCall;
    const description = [type && FUNDING_CALL_TYPE_LABELS[type], provider].filter(Boolean).join(" · ");

    return (
        <>
            <PageHeader
                title={fundingCall.title}
                description={description || undefined}
                meta={(
                    <p className={styles.headerMeta}>
                        {status && <Badge tone={STATUS_TONES[status]}>{FUNDING_CALL_STATUS_LABELS[status]}</Badge>}
                        {publishedAt && <span>Objavljeno {formatDate(publishedAt)}</span>}
                    </p>
                )}
            />
            <div className={styles.layout}>
                <div className={styles.main}>
                    <FundingCallStatusNotice fundingCall={fundingCall}/>
                    <FundingCallFacts fundingCall={fundingCall}/>
                    <FundingCallSections fundingCall={fundingCall}>
                        <EligibilityPanel fundingCallId={fundingCall.id}/>
                    </FundingCallSections>
                </div>
                <FundingCallAside fundingCall={fundingCall}/>
            </div>
        </>
    );
};
