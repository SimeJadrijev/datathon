import type {ReactNode} from "react";
import {AiBlock, Card, ExternalLink} from "@/components";
import type {FundingCallDetail} from "@/types";
import {classNames} from "@/utils";
import {getAttachmentName, isAiField} from "./detailFormatting.ts";
import styles from "./FundingCallDetailPage.module.css";

interface SectionProps {
    id: string;
    title: string;
    children: ReactNode;
}

const Section = ({id, title, children}: SectionProps) => (
    <Card as="section" aria-labelledby={id}>
        <h2 id={id} className={styles.sectionTitle}>{title}</h2>
        {children}
    </Card>
);

interface ConditionsSummaryProps {
    fundingCall: FundingCallDetail;
}

const ConditionsSummary = ({fundingCall}: ConditionsSummaryProps) => {
    const {conditionsSummary} = fundingCall;
    if (!conditionsSummary) {
        return (
            <p className={styles.muted}>
                Za ovaj natječaj još nemamo sažetak. Uvjete pročitaj u službenoj objavi i prilozima.
            </p>
        );
    }
    if (!isAiField(fundingCall, "conditionsSummary")) {
        return <p className={styles.preLine}>{conditionsSummary}</p>;
    }
    return (
        <AiBlock label="AI sažetak" note="Sažetak je napisao AI iz teksta objave. Mjerodavni su samo službeni dokumenti natječaja.">
            {conditionsSummary}
        </AiBlock>
    );
};

interface FundingCallSectionsProps {
    fundingCall: FundingCallDetail;
    // The eligibility check sits right after the conditions it is about.
    children?: ReactNode;
}

// Documents and attachments show only when the source lists them; an empty block would only add noise.
export const FundingCallSections = ({fundingCall, children}: FundingCallSectionsProps) => {
    const {requiredDocuments, attachments} = fundingCall;
    const areDocumentsAi = isAiField(fundingCall, "requiredDocuments");

    return (
        <>
            <Section id="conditions-title" title="Sažetak uvjeta">
                <ConditionsSummary fundingCall={fundingCall}/>
            </Section>
            {children}
            {requiredDocuments.length > 0 && (
                <Section id="documents-title" title="Potrebni dokumenti">
                    <ul className={classNames(styles.list, areDocumentsAi && styles.aiList)}>
                        {requiredDocuments.map((document) => <li key={document}>{document}</li>)}
                    </ul>
                    {areDocumentsAi && (
                        <p className={styles.aiNote}>Popis je izdvojio AI iz teksta objave. Provjeri ga u službenoj dokumentaciji.</p>
                    )}
                </Section>
            )}
            {attachments.length > 0 && (
                <Section id="attachments-title" title="Prilozi">
                    <ul className={styles.attachmentList}>
                        {attachments.map(({name, url}) => (
                            <li key={url}>
                                <ExternalLink href={url}>{getAttachmentName(name, url)}</ExternalLink>
                            </li>
                        ))}
                    </ul>
                </Section>
            )}
        </>
    );
};
