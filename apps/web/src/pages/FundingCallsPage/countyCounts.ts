import {ALL_COUNTIES, COUNTIES, findCounty} from "@/constants";
import type {FacetCount} from "@/types";

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
