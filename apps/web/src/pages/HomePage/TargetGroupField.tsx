import type {Ref} from "react";
import {TARGET_GROUP_DESCRIPTIONS, TARGET_GROUP_LABELS, TARGET_GROUPS} from "@/constants";
import type {TargetGroup} from "@/types";
import {classNames} from "@/utils";
import styles from "./ProfileForm.module.css";

const ERROR_ID = "target-group-error";

interface TargetGroupFieldProps {
    value: TargetGroup | "";
    error?: string;
    onChange: (targetGroup: TargetGroup | "") => void;
    ref?: Ref<HTMLFieldSetElement>;
}

export const TargetGroupField = ({value, error, onChange, ref}: TargetGroupFieldProps) => {
    const handleClick = (targetGroup: TargetGroup) => {
        if (value === targetGroup) {
            onChange("");
        }
    };

    return (
        <fieldset ref={ref} className={styles.fieldset} tabIndex={-1} aria-describedby={error ? ERROR_ID : undefined}>
            <legend className={styles.legend}>
                <span className={styles.step} aria-hidden="true">1</span>
                Tko si?
            </legend>
            <div className={styles.optionGrid}>
                {TARGET_GROUPS.map((targetGroup) => (
                    <label
                        key={targetGroup}
                        className={classNames(styles.option, value === targetGroup && styles.optionSelected)}
                    >
                        <input
                            type="radio"
                            name="targetGroup"
                            value={targetGroup}
                            checked={value === targetGroup}
                            onChange={() => onChange(targetGroup)}
                            onClick={() => handleClick(targetGroup)}
                            className="visually-hidden"
                        />
                        <span className={styles.optionLabel}>{TARGET_GROUP_LABELS[targetGroup]}</span>
                        <span className={styles.optionDescription}>{TARGET_GROUP_DESCRIPTIONS[targetGroup]}</span>
                    </label>
                ))}
            </div>
            {error && <p id={ERROR_ID} className={styles.error}>{error}</p>}
        </fieldset>
    );
};
