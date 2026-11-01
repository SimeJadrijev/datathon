import type {Ref} from "react";
import {Icon} from "@/components";
import {AREA_LABELS, AREAS} from "@/constants";
import type {Area} from "@/types";
import {classNames} from "@/utils";
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
                {AREAS.map((area) => {
                    const isSelected = value.includes(area);

                    return (
                        <label key={area} className={classNames(styles.chip, isSelected && styles.chipSelected)}>
                            <input
                                type="checkbox"
                                name="areas"
                                value={area}
                                checked={isSelected}
                                onChange={() => onChange(toggleArea(value, area))}
                                className="visually-hidden"
                            />
                            {isSelected && <Icon name="check" size={14}/>}
                            {AREA_LABELS[area]}
                        </label>
                    );
                })}
            </div>
            {error && <p id={ERROR_ID} className={styles.error}>{error}</p>}
        </fieldset>
    );
};
