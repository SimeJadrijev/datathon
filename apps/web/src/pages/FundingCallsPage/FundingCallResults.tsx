import type {UseQueryResult} from "@tanstack/react-query";
import {
    Button,
    EmptyState,
    ErrorState,
    FundingCallList,
    FundingCallRow,
    ListSkeleton,
    PageNotFoundState,
    Pagination,
} from "@/components";
import {useSettledValue} from "@/hooks";
import type {FundingCallFilter, FundingCallSummary, Page} from "@/types";
import {classNames} from "@/utils";
import {FundingCallsSummary} from "./FundingCallsSummary.tsx";
import styles from "./FundingCallsPage.module.css";

interface FundingCallResultsProps {
    fundingCalls: UseQueryResult<Page<FundingCallSummary>>;
    filter: FundingCallFilter;
    hasFilters: boolean;
    onSortChange: (sort: string) => void;
    onClear: () => void;
    getPageHref: (page: number) => string;
}

export const FundingCallResults = ({
    fundingCalls,
    filter,
    hasFilters,
    onSortChange,
    onClear,
    getPageHref,
}: FundingCallResultsProps) => {
    const {data, isPending, isError, error, refetch, isPlaceholderData} = fundingCalls;
    const summaryFilter = useSettledValue(filter, data, isPlaceholderData);

    if (isPending) {
        return <ListSkeleton label="Učitavamo natječaje…"/>;
    }
    if (isError) {
        return <ErrorState message={error.message} onRetry={() => refetch()}/>;
    }
    if (data.content.length === 0 && data.page > 0) {
        return <PageNotFoundState firstPageHref={getPageHref(0)}/>;
    }

    return (
        <>
            <FundingCallsSummary
                filter={summaryFilter}
                totalElements={data.totalElements}
                hasFilters={hasFilters}
                sort={filter.sort}
                onSortChange={onSortChange}
                onClear={onClear}
            />
            {data.content.length === 0 ? (
                <EmptyState
                    title={hasFilters ? "Nema natječaja za odabrane filtre" : "Trenutno nema otvorenih natječaja"}
                    hint={hasFilters
                        ? "Makni koji filtar, provjeri kako je pojam napisan ili pogledaj i najavljene natječaje."
                        : "Novi natječaji redovito stižu iz izvora. Pogledaj najavljene ili se vrati za koji dan."}
                    actions={hasFilters && <Button variant="secondary" onClick={onClear}>Poništi filtre</Button>}
                />
            ) : (
                <div className={classNames(isPlaceholderData && styles.stale)} aria-busy={isPlaceholderData}>
                    <FundingCallList>
                        {data.content.map((fundingCall) => (
                            <li key={fundingCall.id}>
                                <FundingCallRow fundingCall={fundingCall}/>
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
