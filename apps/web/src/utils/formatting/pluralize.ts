import {LOCALE} from "@/constants";

const numberFormatter = new Intl.NumberFormat(LOCALE);

interface PluralForms {
    one: string;
    few: string;
    many: string;
}

export const pluralize = (count: number, forms: PluralForms) => {
    const lastDigit = count % 10;
    const lastTwoDigits = count % 100;
    const isTeen = lastTwoDigits >= 11 && lastTwoDigits <= 14;

    if (lastDigit === 1 && !isTeen) {
        return forms.one;
    }
    if (lastDigit >= 2 && lastDigit <= 4 && !isTeen) {
        return forms.few;
    }
    return forms.many;
};

export const formatCount = (count: number) => numberFormatter.format(count);
