import {type FormEvent, useEffect, useId, useState} from "react";
import {Icon} from "@/components/Icon";
import {TextInput} from "@/components/TextInput";
import styles from "./SearchField.module.css";

const DEBOUNCE_MS = 300;

interface SearchFieldProps {
    label: string;
    value: string;
    placeholder?: string;
    onChange: (value: string) => void;
}

export const SearchField = ({label, value, placeholder, onChange}: SearchFieldProps) => {
    const inputId = useId();
    const [text, setText] = useState(value);
    const [committedValue, setCommittedValue] = useState(value);

    if (value !== committedValue) {
        setCommittedValue(value);
        setText(value);
    }

    useEffect(() => {
        if (text.trim() === value) {
            return;
        }
        const timeout = window.setTimeout(() => onChange(text.trim()), DEBOUNCE_MS);
        return () => window.clearTimeout(timeout);
    }, [text, value, onChange]);

    const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
        event.preventDefault();
        onChange(text.trim());
    };

    const handleBlur = () => {
        if (text.trim() !== value) {
            onChange(text.trim());
        }
    };

    return (
        <form role="search" className={styles.form} onSubmit={handleSubmit}>
            <label htmlFor={inputId} className="visually-hidden">{label}</label>
            <Icon name="search" className={styles.icon}/>
            <TextInput
                id={inputId}
                type="search"
                name="q"
                value={text}
                placeholder={placeholder}
                autoComplete="off"
                spellCheck={false}
                maxLength={200}
                enterKeyHint="search"
                className={styles.input}
                onChange={(event) => setText(event.target.value)}
                onBlur={handleBlur}
            />
        </form>
    );
};
