import {useSearchParams} from "wouter";
import {
    DEFAULT_FUNDING_CALL_SORT,
    DEFAULT_FUNDING_CALL_STATUS,
    isArea,
    isFundingCallSort,
    isFundingCallStatusFilter,
    isFundingCallType,
    isSourceCode,
    isTargetGroup,
} from "@/constants";
import {routes} from "@/routes";
import type {FundingCallFilter} from "@/types";
import {
    patchSearchParams,
    readPageParam,
    readSearchParam,
    readTextParam,
    type SearchParamPatch,
    toHref,
    toPageParam,
} from "@/utils";

export const FUNDING_CALL_PAGE_SIZE = 20;

const MAX_QUERY_LENGTH = 200;

type FundingCallFilterKey = "q" | "status" | "targetGroup" | "area" | "county" | "source" | "type" | "sort";

export type FundingCallFilterPatch = Partial<Record<FundingCallFilterKey, string>>;

const parseFilter = (params: URLSearchParams): FundingCallFilter => ({
    q: readTextParam(params, "q", MAX_QUERY_LENGTH),
    status: readSearchParam(params, "status", isFundingCallStatusFilter),
    targetGroup: readSearchParam(params, "targetGroup", isTargetGroup),
    area: readSearchParam(params, "area", isArea),
    county: readTextParam(params, "county", MAX_QUERY_LENGTH),
    source: readSearchParam(params, "source", isSourceCode),
    type: readSearchParam(params, "type", isFundingCallType),
    sort: readSearchParam(params, "sort", isFundingCallSort),
    page: readPageParam(params),
    size: FUNDING_CALL_PAGE_SIZE,
});

// Defaults stay out of the URL, so the plain list keeps the short address /natjecaji.
const withoutDefaults = (patch: FundingCallFilterPatch): SearchParamPatch => ({
    ...patch,
    ...("status" in patch && patch.status === DEFAULT_FUNDING_CALL_STATUS && {status: undefined}),
    ...("sort" in patch && patch.sort === DEFAULT_FUNDING_CALL_SORT && {sort: undefined}),
});

// Filter state lives in the query string: a copied link shows the same list and the back button works.
export const useFundingCallFilter = () => {
    const [searchParams, setSearchParams] = useSearchParams();
    const filter = parseFilter(searchParams);
    const {q, status, targetGroup, area, county, source, type} = filter;
    // Counts do not depend on the page or the order.
    const facetFilter: FundingCallFilter = {q, status, targetGroup, area, county, source, type};
    const activeCount = [q, status, targetGroup, area, county, source, type].filter(Boolean).length;

    // Any change of the filters starts again at the first page. Typing replaces the history entry.
    const updateFilter = (patch: FundingCallFilterPatch, options?: {replace?: boolean}) =>
        setSearchParams((current) => patchSearchParams(current, {...withoutDefaults(patch), page: undefined}), options);

    const clearFilters = () =>
        setSearchParams((current) => patchSearchParams(new URLSearchParams(), {sort: current.get("sort") ?? undefined}));

    const getPageHref = (page: number) =>
        toHref(routes.FUNDING_CALLS, patchSearchParams(searchParams, {page: toPageParam(page)}));

    return {filter, facetFilter, activeCount, updateFilter, clearFilters, getPageHref};
};
