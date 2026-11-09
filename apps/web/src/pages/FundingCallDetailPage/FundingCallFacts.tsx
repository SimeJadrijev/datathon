import {Card, type Fact, FactList, UnknownValue} from "@/components";
import {AREA_LABELS, formatCounty, TARGET_GROUP_LABELS} from "@/constants";
import type {FundingCallDetail} from "@/types";
import {formatAmount, formatDate, formatDeadline} from "@/utils";
import {formatAmountRange, isAiField} from "./detailFormatting.ts";
import styles from "./FundingCallDetailPage.module.css";

const TITLE_ID = "funding-call-facts-title";

const joinOrUnknown = (values: string[]) => (values.length > 0 ? values.join(", ") : <UnknownValue/>);

const formatDeadlineFact = ({deadline, deadlineNote}: FundingCallDetail) => {
    if (deadline) {
        return `${formatDate(deadline)} (${formatDeadline(deadline)})`;
    }
    return deadlineNote ?? <UnknownValue/>;
};

const buildFacts = (fundingCall: FundingCallDetail): Fact[] => {
    const {deadline, totalAmount, targetGroups, areas, counties} = fundingCall;
    const amountRange = formatAmountRange(fundingCall);

    return [
        {
            label: "Rok prijave",
            value: formatDeadlineFact(fundingCall),
            isAi: deadline !== null && isAiField(fundingCall, "deadline"),
        },
        {
            label: "Iznos po prijavi",
            value: amountRange ?? <UnknownValue/>,
            isAi: amountRange !== undefined && isAiField(fundingCall, "minAmount", "maxAmount"),
        },
        {
            label: "Ukupno na raspolaganju",
            value: totalAmount !== null ? formatAmount(totalAmount) : <UnknownValue/>,
            isAi: totalAmount !== null && isAiField(fundingCall, "totalAmount"),
        },
        {
            label: "Kome je namijenjen",
            value: joinOrUnknown(targetGroups.map((group) => TARGET_GROUP_LABELS[group])),
            isAi: targetGroups.length > 0 && isAiField(fundingCall, "targetGroups"),
        },
        {
            label: "Područje",
            value: joinOrUnknown(areas.map((area) => AREA_LABELS[area])),
            isAi: areas.length > 0 && isAiField(fundingCall, "areas"),
        },
        {
            label: "Gdje",
            value: joinOrUnknown(counties.map(formatCounty)),
            isAi: counties.length > 0 && isAiField(fundingCall, "counties"),
        },
    ];
};

interface FundingCallFactsProps {
    fundingCall: FundingCallDetail;
}

export const FundingCallFacts = ({fundingCall}: FundingCallFactsProps) => {
    const facts = buildFacts(fundingCall);
    const hasAiFacts = facts.some((fact) => fact.isAi);

    return (
        <Card as="section" aria-labelledby={TITLE_ID}>
            <h2 id={TITLE_ID} className={styles.sectionTitle}>Osnovni podaci</h2>
            <FactList facts={facts}/>
            {hasAiFacts && (
                <p className={styles.aiNote}>
                    Podcrtane vrijednosti pročitao je AI iz teksta objave
                    {fundingCall.fieldsReviewed ? ", a naš tim ih je ručno pregledao" : ""}.
                    Prije prijave ih provjeri u službenoj objavi.
                </p>
            )}
        </Card>
    );
};
