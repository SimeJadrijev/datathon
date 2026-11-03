import {
    ChoiceChip,
    FilterGrid,
    FilterPanel,
    FilterSelect,
    findCount,
    SearchField,
    toFilterOptions,
} from "@/components";
import {
    ALL_COUNTIES,
    AREA_LABELS,
    AREAS,
    COUNTIES,
    DEFAULT_FUNDING_CALL_STATUS,
    formatCounty,
    FUNDING_CALL_STATUS_FILTER_LABELS,
    FUNDING_CALL_STATUS_FILTERS,
    FUNDING_CALL_TYPE_LABELS,
    FUNDING_CALL_TYPES,
    isArea,
    isFundingCallType,
    isSourceCode,
    isTargetGroup,
    SOURCE_CODES,
    SOURCE_SHORT_NAMES,
    TARGET_GROUP_LABELS,
    TARGET_GROUPS,
} from "@/constants";
import type {FundingCallFacets, FundingCallFilter, FundingCallFilterPatch} from "@/types";
import {toCountyCounts} from "./countyCounts.ts";
import styles from "./FundingCallsPage.module.css";

const FUNDING_SOURCES = SOURCE_CODES.filter((code) => code !== "NARODNE_NOVINE" && code !== "SUDREG");

const getTargetGroupLabel = (value: string) => (isTargetGroup(value) ? TARGET_GROUP_LABELS[value] : undefined);
const getAreaLabel = (value: string) => (isArea(value) ? AREA_LABELS[value] : undefined);
const getSourceLabel = (value: string) => (isSourceCode(value) ? SOURCE_SHORT_NAMES[value] : undefined);
const getTypeLabel = (value: string) => (isFundingCallType(value) ? FUNDING_CALL_TYPE_LABELS[value] : undefined);

// "Cijela Hrvatska" first: most calls are national, and it reads as the natural first choice.
const withWholeCountryFirst = <T extends {value: string}>(options: T[]) => [
    ...options.filter((option) => option.value === ALL_COUNTIES),
    ...options.filter((option) => option.value !== ALL_COUNTIES),
];

interface FundingCallFiltersProps {
    filter: FundingCallFilter;
    facets: FundingCallFacets | undefined;
    activeCount: number;
    onChange: (patch: FundingCallFilterPatch, options?: {replace?: boolean}) => void;
}

export const FundingCallFilters = ({filter, facets, activeCount, onChange}: FundingCallFiltersProps) => {
    const status = filter.status ?? DEFAULT_FUNDING_CALL_STATUS;
    const handleSearch = (q: string) => onChange({q}, {replace: true});

    return (
        <FilterPanel
            activeCount={activeCount}
            search={(
                <SearchField
                    label="Pretraži natječaje"
                    value={filter.q ?? ""}
                    placeholder="npr. „solarne elektrane”"
                    onChange={handleSearch}
                />
            )}
        >
            <fieldset className={styles.statusGroup}>
                <legend className={styles.statusLegend}>Status</legend>
                <div className={styles.chipList}>
                    {FUNDING_CALL_STATUS_FILTERS.map((option) => (
                        <ChoiceChip
                            key={option}
                            type="radio"
                            name="status"
                            value={option}
                            isChecked={status === option}
                            count={findCount(facets?.status, option)}
                            onChange={() => onChange({status: option})}
                        >
                            {FUNDING_CALL_STATUS_FILTER_LABELS[option]}
                        </ChoiceChip>
                    ))}
                </div>
            </fieldset>
            <FilterGrid>
                <FilterSelect
                    label="Kome je namijenjen"
                    name="targetGroup"
                    allLabel="Sve skupine"
                    value={filter.targetGroup}
                    options={toFilterOptions({
                        values: TARGET_GROUPS,
                        counts: facets?.targetGroup,
                        getLabel: getTargetGroupLabel,
                        selected: filter.targetGroup,
                    })}
                    onChange={(targetGroup) => onChange({targetGroup})}
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
                <FilterSelect
                    label="Županija"
                    name="county"
                    allLabel="Sve županije"
                    value={filter.county}
                    options={withWholeCountryFirst(toFilterOptions({
                        values: [ALL_COUNTIES, ...COUNTIES],
                        counts: toCountyCounts(facets?.county),
                        getLabel: formatCounty,
                        selected: filter.county,
                    }))}
                    onChange={(county) => onChange({county})}
                />
                <FilterSelect
                    label="Vrsta"
                    name="type"
                    allLabel="Sve vrste"
                    value={filter.type}
                    options={toFilterOptions({
                        values: FUNDING_CALL_TYPES,
                        counts: facets?.type,
                        getLabel: getTypeLabel,
                        selected: filter.type,
                    })}
                    onChange={(type) => onChange({type})}
                />
                <FilterSelect
                    label="Izvor"
                    name="source"
                    allLabel="Svi izvori"
                    value={filter.source}
                    options={toFilterOptions({
                        values: FUNDING_SOURCES,
                        counts: facets?.source,
                        getLabel: getSourceLabel,
                        selected: filter.source,
                    })}
                    onChange={(source) => onChange({source})}
                />
            </FilterGrid>
        </FilterPanel>
    );
};
