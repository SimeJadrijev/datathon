import type {EligibilityAnswer, EligibilityQuestion} from "@/types";

export type DraftAnswers = Record<string, string>;

export const YES = "yes";
export const NO = "no";

export const getQuestionInputId = (questionId: string) => `eligibility-${questionId}`;

const THOUSANDS_ONLY = /^\d{1,3}(\.\d{3})+$/;

const parseNumber = (text: string) => {
    const compact = text.replace(/\s/g, "");
    const hasCroatianSeparators = compact.includes(",") || THOUSANDS_ONLY.test(compact);
    const normalized = hasCroatianSeparators ? compact.replace(/\./g, "").replace(",", ".") : compact;
    const value = Number(normalized);
    return compact !== "" && Number.isFinite(value) ? value : undefined;
};

interface ParsedAnswers {
    answers: Record<string, EligibilityAnswer>;
    invalidIds: string[];
}

export const parseAnswers = (questions: EligibilityQuestion[], draft: DraftAnswers): ParsedAnswers => {
    const answers: Record<string, EligibilityAnswer> = {};
    const invalidIds: string[] = [];

    for (const question of questions) {
        const value = draft[question.id]?.trim();
        if (!value) {
            continue;
        }
        if (question.type === "YES_NO") {
            answers[question.id] = value === YES;
        } else if (question.type === "NUMBER") {
            const number = parseNumber(value);
            if (number === undefined) {
                invalidIds.push(question.id);
            } else {
                answers[question.id] = number;
            }
        } else {
            answers[question.id] = value;
        }
    }
    return {answers, invalidIds};
};
