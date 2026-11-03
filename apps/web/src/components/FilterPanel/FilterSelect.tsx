import {type ReactNode, useId} from "react";
import {Select} from "@/components/Select";
import styles from "./FilterSelect.module.css";

export interface FilterOption {
    value: string;
    label: string;
    count?: number;
}

interface FilterSelectProps {
    label: string;
    name: string;
    value: string | undefined;
    allLabel: string;
    options: FilterOption[];
    onChange: (value: string | undefined) => void;
}

const formatOption = ({label, count}: FilterOption) => (count === undefined ? label : `${label} (${count})`);

// Native select with "all" as the first option; counts come from the facets endpoint.
export const FilterSelect = ({label, name, value, allLabel, options, onChange}: FilterSelectProps) => {
    const selectId = useId();

    return (
        <div className={styles.field}>
            <label htmlFor={selectId} className={styles.label}>{label}</label>
            <Select
                id={selectId}
                name={name}
                value={value ?? ""}
                onChange={(event) => onChange(event.target.value || undefined)}
            >
                <option value="">{allLabel}</option>
                {options.map((option) => (
                    <option key={option.value} value={option.value}>{formatOption(option)}</option>
                ))}
            </Select>
        </div>
    );
};

interface FilterGridProps {
    children: ReactNode;
}

// Grid for several FilterSelects: as many columns as fit, one column on phones.
export const FilterGrid = ({children}: FilterGridProps) => <div className={styles.grid}>{children}</div>;
