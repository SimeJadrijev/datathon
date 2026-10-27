import {type FormEvent, useRef, useState} from "react";
import {Button, Card, Select} from "@/components";
import {COUNTIES, formatCounty} from "@/constants";
import type {Profile} from "@/types";
import {AreaField} from "./AreaField.tsx";
import {
    hasErrors,
    type ProfileFormErrors,
    type ProfileFormValues,
    toFormValues,
    toProfile,
    validateProfileForm,
} from "./profileForm.ts";
import {TargetGroupField} from "./TargetGroupField.tsx";
import styles from "./ProfileForm.module.css";

interface ProfileFormProps {
    initialProfile?: Profile;
    onSubmit: (profile: Profile) => void;
    onCancel?: () => void;
}

export const ProfileForm = ({initialProfile, onSubmit, onCancel}: ProfileFormProps) => {
    const [values, setValues] = useState(() => toFormValues(initialProfile));
    const [errors, setErrors] = useState<ProfileFormErrors>({});
    const targetGroupRef = useRef<HTMLFieldSetElement>(null);
    const areasRef = useRef<HTMLFieldSetElement>(null);

    const handleChange = (changes: Partial<ProfileFormValues>) => {
        const nextValues = {...values, ...changes};
        setValues(nextValues);

        if (hasErrors(errors)) {
            setErrors(validateProfileForm(nextValues));
        }
    };

    const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
        event.preventDefault();
        const nextErrors = validateProfileForm(values);
        setErrors(nextErrors);

        if (values.type === "" || hasErrors(nextErrors)) {
            const firstInvalidField = nextErrors.type ? targetGroupRef : areasRef;
            firstInvalidField.current?.focus();
            return;
        }

        onSubmit(toProfile(values.type, values));
    };

    return (
        <Card as="section" className={styles.card}>
            <form className={styles.form} onSubmit={handleSubmit} noValidate>
                <TargetGroupField
                    ref={targetGroupRef}
                    value={values.type}
                    error={errors.type}
                    onChange={(type) => handleChange({type})}
                />
                <AreaField
                    ref={areasRef}
                    value={values.areas}
                    error={errors.areas}
                    onChange={(areas) => handleChange({areas})}
                />
                <fieldset className={styles.fieldset}>
                    <legend className={styles.legend}>
                        <span className={styles.step} aria-hidden="true">3</span>
                        Gdje si? <span className={styles.optional}>(neobavezno)</span>
                    </legend>
                    <div className={styles.inputRow}>
                        <div className={styles.field}>
                            <label htmlFor="county" className={styles.label}>Županija</label>
                            <Select
                                id="county"
                                value={values.county}
                                onChange={(event) => handleChange({county: event.target.value})}
                            >
                                <option value="">Sve županije</option>
                                {COUNTIES.map((county) => (
                                    <option key={county} value={county}>{formatCounty(county)}</option>
                                ))}
                            </Select>
                            <p className={styles.hint}>Uvijek se prikazuju natječaji za cijelu Hrvatsku.</p>
                        </div>
                        <div className={styles.field}>
                            <label htmlFor="keywords" className={styles.label}>Ključne riječi</label>
                            <input
                                id="keywords"
                                type="text"
                                className={styles.input}
                                value={values.keywords}
                                maxLength={200}
                                placeholder="npr. solarni paneli, električni automobil"
                                onChange={(event) => handleChange({keywords: event.target.value})}
                            />
                            <p className={styles.hint}>Odvoji ih zarezom.</p>
                        </div>
                    </div>
                </fieldset>
                <div className={styles.actions}>
                    <Button type="submit">Prikaži moje natječaje</Button>
                    {onCancel && <Button variant="secondary" onClick={onCancel}>Odustani</Button>}
                </div>
            </form>
        </Card>
    );
};
