import type {UseQueryResult} from "@tanstack/react-query";
import {Button, EmptyState, ErrorState, FundingCallList, ListSkeleton, Pagination} from "@/components";
import type {Page, RegulationFilter, RegulationSummary, RegulationType} from "@/types";
import {classNames, formatCount, pluralize} from "@/utils";
import {RegulationRow} from "./RegulationRow.tsx";
import styles from "./RegulationsPage.module.css";

const TYPE_PHRASES: Record<RegulationType | "ALL", {one: string; few: string; many: string}> = {
    ALL: {one: "propis", few: "propisa", many: "propisa"},
    LAW: {one: "zakon", few: "zakona", many: "zakona"},
    DECREE: {one: "uredba", few: "uredbe", many: "uredbi"},
    ORDINANCE: {one: "pravilnik", few: "pravilnika", many: "pravilnika"},
    DECISION: {one: "odluka", few: "odluke", many: "odluka"},
    OTHER: {one: "ostali propis", few: "ostala propisa", many: "ostalih propisa"},
};

interface RegulationResultsProps {
    regulations: UseQueryResult<Page<RegulationSummary>>;
    filter: RegulationFilter;
    hasFilters: boolean;
    onClear: () => void;
    getPageHref: (page: number) => string;
}

export const RegulationResults = ({regulations, filter, hasFilters, onClear, getPageHref}: RegulationResultsProps) => {
    const {data, isPending, isError, error, refetch, isPlaceholderData} = regulations;

    if (isPending) {
        return <ListSkeleton label="Učitavamo propise…"/>;
    }
    if (isError) {
        return <ErrorState message={error.message} onRetry={() => refetch()}/>;
    }

    return (
        <>
            <div className={styles.summaryRow}>
                <h2 className={styles.summary} aria-live="polite">
                    <strong className="tabular-nums">{formatCount(data.totalElements)}</strong>{" "}
                    {pluralize(data.totalElements, TYPE_PHRASES[filter.type ?? "ALL"])}
                    {filter.q ? ` za „${filter.q}”` : ""}, najnoviji prvi
                </h2>
                {hasFilters && <Button variant="ghost" size="sm" onClick={onClear}>Poništi filtre</Button>}
            </div>
            {data.content.length === 0 ? (
                <EmptyState
                    icon="document"
                    title="Nema propisa za odabrane filtre"
                    hint="Proširi razdoblje objave, makni koji filtar ili pretraži drugom riječi iz naslova."
                    actions={hasFilters && <Button variant="secondary" onClick={onClear}>Poništi filtre</Button>}
                />
            ) : (
                <div className={classNames(isPlaceholderData && styles.stale)} aria-busy={isPlaceholderData}>
                    <FundingCallList>
                        {data.content.map((regulation) => (
                            <li key={regulation.id}>
                                <RegulationRow regulation={regulation}/>
                            </li>
                        ))}
                    </FundingCallList>
                    <Pagination
                        page={data.page}
                        pageSize={data.size}
                        totalElements={data.totalElements}
                        getPageHref={getPageHref}
                    />
                </div>
            )}
        </>
    );
};
