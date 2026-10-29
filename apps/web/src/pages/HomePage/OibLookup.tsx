import {type FormEvent, useState} from "react";
import {ApiError} from "@/api";
import {Button, Notice, TextInput} from "@/components";
import {useProfileFromOib} from "@/hooks";
import type {OibProfile} from "@/types";
import {isValidOib} from "@/utils";
import styles from "./OibLookup.module.css";

const OIB_LENGTH = 11;
const INVALID_OIB_MESSAGE = "OIB nije valjan. Provjeri je li upisano svih 11 znamenki.";
const MANUAL_FALLBACK = "Možeš nastaviti i ručno ispuniti obrazac ispod.";

const getErrorMessage = (error: Error) => {
    const status = error instanceof ApiError ? error.status : undefined;
    if (status === 400) {
        return INVALID_OIB_MESSAGE;
    }
    if (status === 404) {
        return "Tvrtka s ovim OIB-om nije pronađena u Sudskom registru.";
    }
    if (status === 502) {
        return "Sudski registar trenutno nije dostupan.";
    }
    return error.message;
};

const describeCompany = ({companyName, seat, activityCode}: OibProfile) =>
    [companyName, seat.municipality, activityCode && `djelatnost ${activityCode}`].filter(Boolean).join(", ");

interface OibLookupProps {
    onFound: (oibProfile: OibProfile) => void;
}

export const OibLookup = ({onFound}: OibLookupProps) => {
    const [oib, setOib] = useState("");
    const [validationError, setValidationError] = useState<string>();
    const lookup = useProfileFromOib();

    const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
        event.preventDefault();
        if (!isValidOib(oib)) {
            lookup.reset();
            setValidationError(INVALID_OIB_MESSAGE);
            return;
        }

        setValidationError(undefined);
        lookup.mutate(oib, {onSuccess: onFound});
    };

    const handleChange = (value: string) => {
        // Cut after removing spaces and dashes, so a pasted "21173 725829" keeps all 11 digits.
        setOib(value.replace(/\D/g, "").slice(0, OIB_LENGTH));
        setValidationError(undefined);
    };

    const errorMessage = validationError ?? (lookup.error ? getErrorMessage(lookup.error) : undefined);

    return (
        <form className={styles.lookup} onSubmit={handleSubmit} noValidate>
            <label htmlFor="oib" className={styles.label}>Ili upiši OIB tvrtke</label>
            <p id="oib-hint" className={styles.hint}>
                Podatke ćemo dohvatiti iz Sudskog registra i popuniti obrazac. OIB ne spremamo.
            </p>
            <div className={styles.row}>
                <TextInput
                    id="oib"
                    inputMode="numeric"
                    autoComplete="off"
                    className={styles.input}
                    value={oib}
                    placeholder="11 znamenki"
                    aria-describedby={errorMessage ? "oib-hint oib-error" : "oib-hint"}
                    aria-invalid={Boolean(errorMessage)}
                    onChange={(event) => handleChange(event.target.value)}
                />
                <Button type="submit" variant="secondary" disabled={lookup.isPending}>
                    {lookup.isPending ? "Dohvaćam..." : "Dohvati podatke"}
                </Button>
            </div>
            <div aria-live="polite" className={styles.feedback}>
                {errorMessage && (
                    <p id="oib-error" className={styles.error}>
                        {errorMessage} {MANUAL_FALLBACK}
                    </p>
                )}
                {lookup.data && (
                    <Notice tone="success" title={`Pronašli smo: ${describeCompany(lookup.data)}`}>
                        <p>Izvor: Sudski registar. Obrazac je popunjen, provjeri i promijeni što treba.</p>
                    </Notice>
                )}
            </div>
        </form>
    );
};
