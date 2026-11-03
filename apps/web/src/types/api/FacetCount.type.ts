// Mirrors FacetCount from the facets endpoints. A missing value is counted as "UNKNOWN".
export type FacetCount = {
    value: string;
    count: number;
};
