import type {Ref} from "react";
import {ChoiceChip} from "@/components";
import {AREA_LABELS, AREAS} from "@/constants";
import type {Area} from "@/types";
import styles from "./ProfileForm.module.css";

const ERROR_ID = "areas-error";

const toggleArea = (areas: Area[], area: Area) =>
    areas.includes(area) ? areas.filter((selectedArea) => selectedArea !== area) : [...areas, area];

interface AreaFieldProps {
    value: Area[];
    error?: string;
    onChange: (areas: Area[]) => void;
    ref?: Ref<HTMLFieldSetElement>;
}

export const AreaField = ({value, error, onChange, ref}: AreaFieldProps) => {
    return (
        <fieldset ref={ref} className={styles.fieldset} tabIndex={-1} aria-describedby={error ? ERROR_ID : undefined}>
            <legend className={styles.legend}>
                <span className={styles.step} aria-hidden="true">2</span>
                Što te zanima?
            </legend>
            <p className={styles.hint}>Odaberi jedno ili više područja.</p>
            <div className={styles.chipList}>
                {AREAS.map((area) => (
                    <ChoiceChip
                        key={area}
                        type="checkbox"
                        name="areas"
                        value={area}
                        isChecked={value.includes(area)}
                        onChange={() => onChange(toggleArea(value, area))}
                    >
                        {AREA_LABELS[area]}
                    </ChoiceChip>
                ))}
            </div>
            {error && <p id={ERROR_ID} className={styles.error}>{error}</p>}
        </fieldset>
    );
};
