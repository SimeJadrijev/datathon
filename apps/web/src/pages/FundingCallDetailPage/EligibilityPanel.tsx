import {type FormEvent, useState} from "react";
import {ApiError} from "@/api";
import {Button, Card, Icon, Notice} from "@/components";
import {formatCounty, TARGET_GROUP_LABELS} from "@/constants";
import {useGetEligibilityCheck, useGetEligibilityQuestions, useProfile} from "@/hooks";
import type {EligibilityQuestion} from "@/types";
import {type DraftAnswers, getQuestionInputId, parseAnswers} from "./eligibilityAnswers.ts";
import {EligibilityQuestionField} from "./EligibilityQuestionField.tsx";
import {EligibilityResult} from "./EligibilityResult.tsx";
import styles from "./EligibilityPanel.module.css";

const TITLE_ID = "eligibility-title";
const FORM_ERROR_ID = "eligibility-form-error";

const RATE_LIMIT_MESSAGE = "U zadnjoj minuti bilo je previše provjera. Pričekaj minutu pa pokušaj ponovno.";

const getErrorMessage = (error: Error) =>
    error instanceof ApiError && error.status === 429 ? RATE_LIMIT_MESSAGE : error.message;

interface EligibilityFormProps {
    fundingCallId: number;
    questions: EligibilityQuestion[];
}

const EligibilityForm = ({fundingCallId, questions}: EligibilityFormProps) => {
    const {profile} = useProfile();
    const check = useGetEligibilityCheck();
    const [draft, setDraft] = useState<DraftAnswers>({});
    const [invalidIds, setInvalidIds] = useState<string[]>([]);
    const [hasNoAnswers, setHasNoAnswers] = useState(false);

    // A result is about the answers it was given; once they change it would mislead, so it goes away.
    const handleAnswerChange = (questionId: string, value: string) => {
        setDraft((current) => ({...current, [questionId]: value}));
        setInvalidIds((current) => current.filter((id) => id !== questionId));
        setHasNoAnswers(false);
        check.reset();
    };

    const submit = () => {
        const {answers, invalidIds: nextInvalidIds} = parseAnswers(questions, draft);
        setInvalidIds(nextInvalidIds);
        if (nextInvalidIds.length > 0) {
            document.getElementById(getQuestionInputId(nextInvalidIds[0]))?.focus();
            return;
        }
        if (Object.keys(answers).length === 0) {
            setHasNoAnswers(true);
            return;
        }
        check.mutate({fundingCallId, request: {answers, targetGroup: profile?.type, county: profile?.county}});
    };

    const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
        event.preventDefault();
        submit();
    };

    return (
        <>
            <form className={styles.form} noValidate onSubmit={handleSubmit}>
                {questions.map((question, index) => (
                    <EligibilityQuestionField
                        key={question.id}
                        question={question}
                        number={index + 1}
                        value={draft[question.id] ?? ""}
                        isInvalid={invalidIds.includes(question.id)}
                        onChange={(value) => handleAnswerChange(question.id, value)}
                    />
                ))}
                {profile && (
                    <p className={styles.profileNote}>
                        Uz odgovore šaljemo i tvoj profil: {TARGET_GROUP_LABELS[profile.type]}
                        {profile.county ? `, ${formatCounty(profile.county)}` : ""}.
                    </p>
                )}
                <div className={styles.actions}>
                    <Button type="submit" disabled={check.isPending} aria-describedby={hasNoAnswers ? FORM_ERROR_ID : undefined}>
                        {check.isPending ? "Provjeravam…" : "Provjeri"}
                    </Button>
                    {hasNoAnswers && (
                        <p id={FORM_ERROR_ID} className={styles.fieldError} role="alert">Odgovori barem na jedno pitanje.</p>
                    )}
                </div>
            </form>
            <div aria-live="polite">
                {check.isPending && <p className={styles.pending}>Uspoređujemo tvoje odgovore s tekstom poziva…</p>}
                {check.isError && (
                    <Notice
                        tone={check.error instanceof ApiError && check.error.status === 429 ? "warning" : "danger"}
                        title="Provjera nije uspjela"
                        className={styles.errorNotice}
                    >
                        <p>{getErrorMessage(check.error)}</p>
                        <Button variant="secondary" size="sm" className={styles.retry} onClick={submit}>
                            <Icon name="retry"/>
                            Pokušaj ponovno
                        </Button>
                    </Notice>
                )}
                {check.data && <EligibilityResult result={check.data}/>}
            </div>
        </>
    );
};

interface EligibilityPanelProps {
    fundingCallId: number;
}

// The panel shows only for calls with prepared questions; without them there is nothing honest to ask.
export const EligibilityPanel = ({fundingCallId}: EligibilityPanelProps) => {
    const {data: questions} = useGetEligibilityQuestions(fundingCallId);

    if (!questions || questions.length === 0) {
        return null;
    }

    return (
        <Card as="section" aria-labelledby={TITLE_ID} className={styles.panel}>
            <h2 id={TITLE_ID} className={styles.title}>Ispunjavam li uvjete?</h2>
            <p className={styles.intro}>
                Odgovori na pitanja koja znaš, ostala preskoči. AI će tvoje odgovore usporediti s tekstom poziva
                i za svaki zaključak navesti rečenicu iz poziva na koju se oslanja.
            </p>
            <EligibilityForm fundingCallId={fundingCallId} questions={questions}/>
        </Card>
    );
};
