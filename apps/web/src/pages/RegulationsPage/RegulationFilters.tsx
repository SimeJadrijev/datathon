import {FilterGrid, FilterPanel, FilterSelect, SearchField, toFilterOptions} from "@/components";
import {
    AREA_LABELS,
    AREAS,
    isArea,
    isRegulationType,
    REGULATION_PERIOD_LABELS,
    REGULATION_PERIODS,
    REGULATION_TYPE_LABELS,
    REGULATION_TYPES,
} from "@/constants";
import type {RegulationFilterPatch} from "@/hooks";
import type {RegulationFacets, RegulationFilter, RegulationPeriod} from "@/types";

const getTypeLabel = (value: string) => (isRegulationType(value) ? REGULATION_TYPE_LABELS[value] : undefined);
const getAreaLabel = (value: string) => (isArea(value) ? AREA_LABELS[value] : undefined);

const PERIOD_OPTIONS = REGULATION_PERIODS.map((period) => ({value: period, label: REGULATION_PERIOD_LABELS[period]}));

interface RegulationFiltersProps {
    filter: RegulationFilter;
    period: RegulationPeriod | undefined;
    facets: RegulationFacets | undefined;
    activeCount: number;
    onChange: (patch: RegulationFilterPatch, options?: {replace?: boolean}) => void;
}

export const RegulationFilters = ({filter, period, facets, activeCount, onChange}: RegulationFiltersProps) => {
    return (
        <FilterPanel
            activeCount={activeCount}
            search={(
                <SearchField
                    label="Pretraži propise"
                    value={filter.q ?? ""}
                    placeholder="npr. „porez na dohodak”"
                    onChange={(q) => onChange({q}, {replace: true})}
                />
            )}
        >
            <FilterGrid>
                <FilterSelect
                    label="Vrsta propisa"
                    name="type"
                    allLabel="Sve vrste"
                    value={filter.type}
                    options={toFilterOptions({
                        values: REGULATION_TYPES,
                        counts: facets?.type,
                        getLabel: getTypeLabel,
                        selected: filter.type,
                    })}
                    onChange={(type) => onChange({type})}
                />
                <FilterSelect
                    label="Objavljeno"
                    name="period"
                    allLabel="Bilo kada"
                    value={period}
                    options={PERIOD_OPTIONS}
                    onChange={(nextPeriod) => onChange({period: nextPeriod})}
                />
                <FilterSelect
                    label="Područje"
                    name="area"
                    allLabel="Sva područja"
                    value={filter.area}
                    options={toFilterOptions({
                        values: AREAS,
                        counts: facets?.area,
                        getLabel: getAreaLabel,
                        selected: filter.area,
                    })}
                    onChange={(area) => onChange({area})}
                />
            </FilterGrid>
        </FilterPanel>
    );
};
