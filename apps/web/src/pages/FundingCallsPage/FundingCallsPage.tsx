import {ExternalButtonLink, PageHeader} from "@/components";
import {useFundingCallFilter, useGetFundingCallFacets, useGetFundingCalls} from "@/hooks";
import {FundingCallFilters} from "./FundingCallFilters.tsx";
import {FundingCallResults} from "./FundingCallResults.tsx";
import {getFeedHref} from "./feedHref.ts";

export const FundingCallsPage = () => {
    const {filter, facetFilter, activeCount, updateFilter, clearFilters, getPageHref} = useFundingCallFilter();
    const fundingCalls = useGetFundingCalls(filter);
    const facets = useGetFundingCallFacets(facetFilter);

    return (
        <>
            <PageHeader
                title="Svi natječaji"
                description="Natječaji, potpore i stipendije iz službenih izvora na jednom mjestu. Suzi popis filtrima, a poveznicu na pretragu možeš poslati i drugima ili je pratiti u RSS čitaču."
                actions={
                    <ExternalButtonLink href={getFeedHref(filter)} variant="secondary" size="sm">
                        Prati nove (RSS)
                    </ExternalButtonLink>
                }
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
