import {ALL_COUNTIES, COUNTIES, findCounty} from "@/constants";
import type {FacetCount} from "@/types";

// Facet values use the spelling most calls have ("Istarska županija"); the filter and the URL use the short
// name, so both are mapped to it and a county never shows up twice. A county without calls of its own still
// gets the calls for the whole country, which is exactly what filtering by it returns.
export const toCountyCounts = (counts: FacetCount[] | undefined): FacetCount[] | undefined => {
    if (!counts) {
        return undefined;
    }
    const countsByValue = new Map<string, number>();
    for (const {value, count} of counts) {
        const county = findCounty(value) ?? value;
        countsByValue.set(county, Math.max(countsByValue.get(county) ?? 0, count));
    }
    const wholeCountryCount = countsByValue.get(ALL_COUNTIES) ?? 0;
    if (wholeCountryCount > 0) {
        for (const county of COUNTIES) {
            countsByValue.set(county, countsByValue.get(county) ?? wholeCountryCount);
        }
    }
    return [...countsByValue].map(([value, count]) => ({value, count}));
};
