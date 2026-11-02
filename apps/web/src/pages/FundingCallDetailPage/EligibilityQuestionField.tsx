import {ChoiceChip, TextInput} from "@/components";
import type {EligibilityQuestion} from "@/types";
import {getQuestionInputId, NO, YES} from "./eligibilityAnswers.ts";
import styles from "./EligibilityPanel.module.css";

// "Ne znam" clears the answer: a radio cannot be unchecked, and guessing would only mislead the check.
const UNKNOWN = "";

const getOptions = (question: EligibilityQuestion) => {
    if (question.type === "YES_NO") {
        return [{value: YES, label: "Da"}, {value: NO, label: "Ne"}];
    }
    return (question.options ?? []).map((option) => ({value: option, label: option}));
};

interface EligibilityQuestionFieldProps {
    question: EligibilityQuestion;
    number: number;
    value: string;
    isInvalid: boolean;
    onChange: (value: string) => void;
}

export const EligibilityQuestionField = ({question, number, value, isInvalid, onChange}: EligibilityQuestionFieldProps) => {
    const inputId = getQuestionInputId(question.id);
    const errorId = `${inputId}-error`;

    if (question.type === "NUMBER") {
        return (
            <div className={styles.question}>
                <label htmlFor={inputId} className={styles.questionText}>
                    <span className={styles.questionNumber} aria-hidden="true">{number}.</span>
                    {question.text}
                </label>
                <TextInput
                    id={inputId}
                    name={question.id}
                    inputMode="decimal"
                    autoComplete="off"
                    placeholder="npr. 25"
                    value={value}
                    aria-invalid={isInvalid}
                    aria-describedby={isInvalid ? errorId : undefined}
                    className={styles.numberInput}
                    onChange={(event) => onChange(event.target.value)}
                />
                {isInvalid && <p id={errorId} className={styles.fieldError}>Upiši broj znamenkama, npr. 3.</p>}
            </div>
        );
    }

    return (
        <fieldset className={styles.question}>
            <legend className={styles.questionText}>
                <span className={styles.questionNumber} aria-hidden="true">{number}.</span>
                {question.text}
            </legend>
            <div className={styles.choices}>
                {[...getOptions(question), {value: UNKNOWN, label: "Ne znam"}].map((option) => (
                    <ChoiceChip
                        key={option.value || "unknown"}
                        type="radio"
                        name={question.id}
                        value={option.value}
                        isChecked={value === option.value}
                        onChange={() => onChange(option.value)}
                    >
                        {option.label}
                    </ChoiceChip>
                ))}
            </div>
        </fieldset>
    );
};
