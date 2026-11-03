import {PageHeader} from "@/components";
import {useFundingCallFilter, useGetFundingCallFacets, useGetFundingCalls} from "@/hooks";
import {FundingCallFilters} from "./FundingCallFilters.tsx";
import {FundingCallResults} from "./FundingCallResults.tsx";

export const FundingCallsPage = () => {
    const {filter, facetFilter, activeCount, updateFilter, clearFilters, getPageHref} = useFundingCallFilter();
    const fundingCalls = useGetFundingCalls(filter);
    const facets = useGetFundingCallFacets(facetFilter);

    return (
        <>
            <PageHeader
                title="Svi natječaji"
                description="Natječaji, potpore i stipendije iz šest službenih izvora na jednom mjestu. Suzi popis filtrima, a poveznicu na pretragu možeš poslati i drugima."
            />
            <FundingCallFilters
                filter={filter}
                facets={facets.data}
                activeCount={activeCount}
                onChange={updateFilter}
            />
            <FundingCallResults
                fundingCalls={fundingCalls}
                filter={filter}
                hasFilters={activeCount > 0}
                onSortChange={(sort) => updateFilter({sort})}
                onClear={clearFilters}
                getPageHref={getPageHref}
            />
        </>
    );
};
