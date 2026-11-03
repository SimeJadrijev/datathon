import {PageHeader} from "@/components";
import {useGetRegulationFacets, useGetRegulations, useRegulationFilter} from "@/hooks";
import {RegulationFilters} from "./RegulationFilters.tsx";
import {RegulationResults} from "./RegulationResults.tsx";

export const RegulationsPage = () => {
    const {filter, facetFilter, period, activeCount, updateFilter, clearFilters, getPageHref} = useRegulationFilter();
    const regulations = useGetRegulations(filter);
    const facets = useGetRegulationFacets(facetFilter);

    return (
        <>
            <PageHeader
                title="Zakoni i propisi"
                description="Zakoni, uredbe, pravilnici i odluke iz Narodnih novina, s poveznicom na službeni tekst. Propisi iz zadnjih 30 dana označeni su kao novi."
            />
            <RegulationFilters
                filter={filter}
                period={period}
                facets={facets.data}
                activeCount={activeCount}
                onChange={updateFilter}
            />
            <RegulationResults
                regulations={regulations}
                filter={filter}
                hasFilters={activeCount > 0}
                onClear={clearFilters}
                getPageHref={getPageHref}
            />
        </>
    );
};
