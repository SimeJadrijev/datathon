import {Button, Select} from "@/components";
import {
    DEFAULT_FUNDING_CALL_SORT,
    DEFAULT_FUNDING_CALL_STATUS,
    FUNDING_CALL_SORT_LABELS,
    FUNDING_CALL_SORTS,
    isFundingCallSort,
} from "@/constants";
import type {FundingCallFilter, FundingCallSort, FundingCallStatusFilter} from "@/types";
import {formatCount, pluralize} from "@/utils";
import styles from "./FundingCallsPage.module.css";

const STATUS_PHRASES: Record<FundingCallStatusFilter, {one: string; few: string; many: string}> = {
    OPEN: {one: "otvoren natječaj", few: "otvorena natječaja", many: "otvorenih natječaja"},
    ANNOUNCED: {one: "najavljen natječaj", few: "najavljena natječaja", many: "najavljenih natječaja"},
    CLOSED: {one: "zatvoren natječaj", few: "zatvorena natječaja", many: "zatvorenih natječaja"},
    UNKNOWN: {
        one: "natječaj bez potvrđenog statusa",
        few: "natječaja bez potvrđenog statusa",
        many: "natječaja bez potvrđenog statusa",
    },
};

const SORT_SELECT_ID = "funding-call-sort";

const describeScope = ({q, targetGroup, area, county, source, type}: FundingCallFilter) => {
    const hasSelectFilters = Boolean(targetGroup || area || county || source || type);
    if (q && hasSelectFilters) {
        return ` za „${q}” uz odabrane filtre`;
    }
    if (q) {
        return ` za „${q}”`;
    }
    return hasSelectFilters ? " za odabrane filtre" : "";
};

interface FundingCallsSummaryProps {
    filter: FundingCallFilter;
    totalElements: number;
    hasFilters: boolean;
    sort: FundingCallSort | undefined;
    onSortChange: (sort: string) => void;
    onClear: () => void;
}

// One plain sentence with the real count, then sorting and the reset on the same line.
export const FundingCallsSummary = ({
    filter,
    totalElements,
    hasFilters,
    sort,
    onSortChange,
    onClear,
}: FundingCallsSummaryProps) => {
    const status = filter.status ?? DEFAULT_FUNDING_CALL_STATUS;

    return (
        <div className={styles.summaryRow}>
            <h2 className={styles.summary} aria-live="polite">
                <strong className="tabular-nums">{formatCount(totalElements)}</strong>{" "}
                {pluralize(totalElements, STATUS_PHRASES[status])}
                {describeScope(filter)}
            </h2>
            <div className={styles.summaryActions}>
                {hasFilters && <Button variant="ghost" size="sm" onClick={onClear}>Poništi filtre</Button>}
                <label htmlFor={SORT_SELECT_ID} className={styles.sortLabel}>Poredaj po</label>
                <Select
                    id={SORT_SELECT_ID}
                    name="sort"
                    className={styles.sortSelect}
                    value={sort ?? DEFAULT_FUNDING_CALL_SORT}
                    onChange={(event) => isFundingCallSort(event.target.value) && onSortChange(event.target.value)}
                >
                    {FUNDING_CALL_SORTS.map((sort) => (
                        <option key={sort} value={sort}>{FUNDING_CALL_SORT_LABELS[sort]}</option>
                    ))}
                </Select>
            </div>
        </div>
    );
};
