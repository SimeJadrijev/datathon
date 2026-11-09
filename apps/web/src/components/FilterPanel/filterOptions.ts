import {LOCALE} from "@/constants";
import type {FacetCount} from "@/types";
import type {FilterOption} from "./FilterSelect.tsx";

interface FilterOptionsInput {
    values: readonly string[];
    counts: FacetCount[] | undefined;
    getLabel: (value: string) => string | undefined;
    selected?: string;
}

const byLabel = (first: FilterOption, second: FilterOption) => first.label.localeCompare(second.label, LOCALE);

export const toFilterOptions = ({values, counts, getLabel, selected}: FilterOptionsInput): FilterOption[] => {
    const toOption = (value: string, count?: number): FilterOption[] => {
        const label = getLabel(value);
        return label ? [{value, label, count}] : [];
    };

    if (!counts) {
        return values.flatMap((value) => toOption(value)).sort(byLabel);
    }

    const options = counts.flatMap(({value, count}) => toOption(value, count));
    if (selected && !options.some((option) => option.value === selected)) {
        options.push(...toOption(selected, 0));
    }
    return options.sort(byLabel);
};

export const findCount = (counts: FacetCount[] | undefined, value: string) =>
    counts ? (counts.find((facet) => facet.value === value)?.count ?? 0) : undefined;
