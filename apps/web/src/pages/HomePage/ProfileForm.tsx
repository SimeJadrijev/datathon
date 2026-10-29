import {type FormEvent, useRef, useState} from "react";
import {Button, Card, Select, TextInput} from "@/components";
import {COUNTIES, formatCounty} from "@/constants";
import type {OibProfile, Profile} from "@/types";
import {AreaField} from "./AreaField.tsx";
import {OibLookup} from "./OibLookup.tsx";
import {
    fromOibProfile,
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
    const keywordsRef = useRef<HTMLInputElement>(null);

    const handleChange = (changes: Partial<ProfileFormValues>) => {
        const nextValues = {...values, ...changes};
        setValues(nextValues);

        if (hasErrors(errors)) {
            setErrors(validateProfileForm(nextValues));
        }
    };

    const handleOibFound = (oibProfile: OibProfile) => {
        setValues(fromOibProfile(oibProfile, values));
        setErrors({});
    };

    const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
        event.preventDefault();
        const nextErrors = validateProfileForm(values);
        setErrors(nextErrors);

        if (values.type === "" || hasErrors(nextErrors)) {
            focusFirstInvalidField(nextErrors);
            return;
        }

        onSubmit(toProfile(values.type, values));
    };

    const focusFirstInvalidField = (nextErrors: ProfileFormErrors) => {
        if (nextErrors.type) {
            targetGroupRef.current?.focus();
            return;
        }
        if (nextErrors.areas) {
            areasRef.current?.focus();
            return;
        }
        keywordsRef.current?.focus();
    };

    return (
        <Card as="section" className={styles.card} aria-labelledby="profile-form-title">
            <header>
                <h2 id="profile-form-title" className={styles.title}>Tvoj profil</h2>
                <p className={styles.intro}>Manje od minute. Profil ostaje samo u ovom pregledniku.</p>
            </header>
            <OibLookup onFound={handleOibFound}/>
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
                            <TextInput
                                id="keywords"
                                ref={keywordsRef}
                                value={values.keywords}
                                maxLength={200}
                                placeholder="npr. solarni paneli, električni automobil"
                                aria-describedby={errors.keywords ? "keywords-hint keywords-error" : "keywords-hint"}
                                aria-invalid={Boolean(errors.keywords)}
                                onChange={(event) => handleChange({keywords: event.target.value})}
                            />
                            <p id="keywords-hint" className={styles.hint}>Odvoji ih zarezom.</p>
                            {errors.keywords && <p id="keywords-error" className={styles.error}>{errors.keywords}</p>}
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
