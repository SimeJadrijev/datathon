import {useSearchParams} from "wouter";
import {
    DEFAULT_FUNDING_CALL_SORT,
    DEFAULT_FUNDING_CALL_STATUS,
    findCounty,
    FUNDING_CALL_PAGE_SIZE,
    isArea,
    isFundingCallSort,
    isFundingCallStatusFilter,
    isFundingCallType,
    isSourceCode,
    isTargetGroup,
} from "@/constants";
import {routes} from "@/routes";
import type {FundingCallFilter, FundingCallFilterPatch} from "@/types";
import {
    patchSearchParams,
    readPageParam,
    readSearchParam,
    readTextParam,
    type SearchParamPatch,
    toHref,
    toPageParam,
} from "@/utils";

const MAX_QUERY_LENGTH = 200;

const readCountyParam = (params: URLSearchParams) => {
    const county = readTextParam(params, "county", MAX_QUERY_LENGTH);
    return county && (findCounty(county) ?? county);
};

const parseFilter = (params: URLSearchParams): FundingCallFilter => ({
    q: readTextParam(params, "q", MAX_QUERY_LENGTH),
    status: readSearchParam(params, "status", isFundingCallStatusFilter),
    targetGroup: readSearchParam(params, "targetGroup", isTargetGroup),
    area: readSearchParam(params, "area", isArea),
    county: readCountyParam(params),
    source: readSearchParam(params, "source", isSourceCode),
    type: readSearchParam(params, "type", isFundingCallType),
    sort: readSearchParam(params, "sort", isFundingCallSort),
    page: readPageParam(params),
    size: FUNDING_CALL_PAGE_SIZE,
});

const withoutDefaults = (patch: FundingCallFilterPatch): SearchParamPatch => ({
    ...patch,
    ...("status" in patch && patch.status === DEFAULT_FUNDING_CALL_STATUS && {status: undefined}),
    ...("sort" in patch && patch.sort === DEFAULT_FUNDING_CALL_SORT && {sort: undefined}),
});

export const useFundingCallFilter = () => {
    const [searchParams, setSearchParams] = useSearchParams();
    const filter = parseFilter(searchParams);
    const {q, status, targetGroup, area, county, source, type} = filter;
    const facetFilter: FundingCallFilter = {q, status, targetGroup, area, county, source, type};
    const activeStatus = status === DEFAULT_FUNDING_CALL_STATUS ? undefined : status;
    const activeCount = [q, activeStatus, targetGroup, area, county, source, type].filter(Boolean).length;

    const updateFilter = (patch: FundingCallFilterPatch, options?: {replace?: boolean}) =>
        setSearchParams((current) => patchSearchParams(current, {...withoutDefaults(patch), page: undefined}), options);

    const clearFilters = () =>
        setSearchParams((current) => patchSearchParams(new URLSearchParams(), {sort: current.get("sort") ?? undefined}));

    const getPageHref = (page: number) =>
        toHref(routes.FUNDING_CALLS, patchSearchParams(searchParams, {page: toPageParam(page)}));

    return {filter, facetFilter, activeCount, updateFilter, clearFilters, getPageHref};
};
