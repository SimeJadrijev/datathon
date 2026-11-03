import {useSearchParams} from "wouter";
import {isArea, isRegulationPeriod, isRegulationType, REGULATION_PAGE_SIZE} from "@/constants";
import {routes} from "@/routes";
import type {RegulationFilter, RegulationFilterPatch, RegulationPeriod} from "@/types";
import {patchSearchParams, readPageParam, readSearchParam, readTextParam, toHref, toIsoDate, toPageParam} from "@/utils";

const MAX_QUERY_LENGTH = 200;

const daysAgo = (days: number, today: Date) =>
    new Date(today.getFullYear(), today.getMonth(), today.getDate() - days);

// The URL keeps the period ("?period=30d"), so a shared link still means "the last 30 days" a week later.
const getPublishedRange = (period: RegulationPeriod | undefined, today = new Date()) => {
    const year = today.getFullYear();
    switch (period) {
        case "30d":
            return {publishedFrom: toIsoDate(daysAgo(30, today))};
        case "90d":
            return {publishedFrom: toIsoDate(daysAgo(90, today))};
        case "thisYear":
            return {publishedFrom: toIsoDate(new Date(year, 0, 1))};
        case "lastYear":
            return {publishedFrom: toIsoDate(new Date(year - 1, 0, 1)), publishedTo: toIsoDate(new Date(year - 1, 11, 31))};
        default:
            return {};
    }
};

export const useRegulationFilter = () => {
    const [searchParams, setSearchParams] = useSearchParams();
    const period = readSearchParam(searchParams, "period", isRegulationPeriod);
    const filter: RegulationFilter = {
        q: readTextParam(searchParams, "q", MAX_QUERY_LENGTH),
        type: readSearchParam(searchParams, "type", isRegulationType),
        area: readSearchParam(searchParams, "area", isArea),
        ...getPublishedRange(period),
        page: readPageParam(searchParams),
        size: REGULATION_PAGE_SIZE,
    };
    // Counts do not depend on the page.
    const {q, type, area, publishedFrom, publishedTo} = filter;
    const facetFilter: RegulationFilter = {q, type, area, publishedFrom, publishedTo};
    const activeCount = [filter.q, filter.type, filter.area, period].filter(Boolean).length;

    const updateFilter = (patch: RegulationFilterPatch, options?: {replace?: boolean}) =>
        setSearchParams((current) => patchSearchParams(current, {...patch, page: undefined}), options);

    const clearFilters = () => setSearchParams(new URLSearchParams());

    const getPageHref = (page: number) =>
        toHref(routes.REGULATIONS, patchSearchParams(searchParams, {page: toPageParam(page)}));

    return {filter, facetFilter, period, activeCount, updateFilter, clearFilters, getPageHref};
};
