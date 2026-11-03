import {LOCALE} from "@/constants";
import type {FacetCount} from "@/types";
import type {FilterOption} from "./FilterSelect.tsx";

interface FilterOptionsInput {
    // Every value the filter can take; used without counts when the facets did not load.
    values: readonly string[];
    counts: FacetCount[] | undefined;
    getLabel: (value: string) => string | undefined;
    selected?: string;
}

const byLabel = (first: FilterOption, second: FilterOption) => first.label.localeCompare(second.label, LOCALE);

// With counts, only values that have results are offered (no dead ends), plus the selected one even at 0.
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
