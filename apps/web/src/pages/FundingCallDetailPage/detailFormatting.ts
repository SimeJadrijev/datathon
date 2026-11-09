import type {FundingCallDetail} from "@/types";
import {formatAmount} from "@/utils";

export const formatAmountRange = ({minAmount, maxAmount}: Pick<FundingCallDetail, "minAmount" | "maxAmount">) => {
    if (minAmount !== null && maxAmount !== null && minAmount !== maxAmount) {
        return `${formatAmount(minAmount)} – ${formatAmount(maxAmount)}`;
    }
    if (minAmount !== null && minAmount === maxAmount) {
        return formatAmount(minAmount);
    }
    if (maxAmount !== null) {
        return `do ${formatAmount(maxAmount)}`;
    }
    if (minAmount !== null) {
        return `od ${formatAmount(minAmount)}`;
    }
    return undefined;
};

export const isAiField = (fundingCall: FundingCallDetail, ...fields: string[]) =>
    fields.some((field) => fundingCall.aiFields.includes(field));

export const getAttachmentName = (name: string | null, url: string) => {
    if (name?.trim()) {
        return name.trim();
    }
    const fileName = url.split("/").filter(Boolean).pop();
    return fileName ? decodeURIComponent(fileName) : "Prilog";
};
