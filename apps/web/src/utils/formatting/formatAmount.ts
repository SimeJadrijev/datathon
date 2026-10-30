import {LOCALE} from "@/constants";

const wholeAmountFormatter = new Intl.NumberFormat(LOCALE, {
    style: "currency",
    currency: "EUR",
    maximumFractionDigits: 0,
});

const fractionalAmountFormatter = new Intl.NumberFormat(LOCALE, {
    style: "currency",
    currency: "EUR",
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
});

export const formatAmount = (amount: number) => {
    const formatter = Number.isInteger(amount) ? wholeAmountFormatter : fractionalAmountFormatter;
    return formatter.format(amount);
};
