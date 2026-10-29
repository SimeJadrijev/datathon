import {LOCALE} from "@/constants";

const numberFormatter = new Intl.NumberFormat(LOCALE);

interface PluralForms {
    one: string;
    few: string;
    many: string;
}

// Croatian plural: 1 natječaj, 2 natječaja, 5 natječaja; 21 behaves like 1, 11 to 14 like 5.
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
