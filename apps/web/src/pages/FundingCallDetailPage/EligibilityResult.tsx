import {ExternalLink, Icon, type IconName} from "@/components";
import type {EligibilityCheckResponse, EligibilityVerdict} from "@/types";
import {classNames} from "@/utils";
import styles from "./EligibilityPanel.module.css";

interface VerdictCopy {
    title: string;
    hint: string;
    icon: IconName;
    className: string | undefined;
}

// Words and an icon carry the verdict; colour only repeats it.
const VERDICTS: Record<EligibilityVerdict, VerdictCopy> = {
    LIKELY_ELIGIBLE: {
        title: "Vjerojatno ispunjavaš uvjete",
        hint: "Tvoji odgovori odgovaraju uvjetima iz teksta poziva.",
        icon: "check",
        className: styles.verdictEligible,
    },
    LIKELY_NOT_ELIGIBLE: {
        title: "Vjerojatno ne ispunjavaš uvjete",
        hint: "Barem jedan odgovor ne odgovara uvjetima iz teksta poziva.",
        icon: "close",
        className: styles.verdictNotEligible,
    },
    UNCLEAR: {
        title: "Ne može se pouzdano procijeniti",
        hint: "Tekst poziva ili tvoji odgovori nisu dovoljni za jasan odgovor.",
        icon: "help",
        className: styles.verdictUnclear,
    },
};

interface EligibilityResultProps {
    result: EligibilityCheckResponse;
}

export const EligibilityResult = ({result}: EligibilityResultProps) => {
    const verdict = VERDICTS[result.verdict];
    // Only model reasons carry a quote; without one the API answered from simple rules, which is not AI.
    const isAi = result.reasons.some((reason) => reason.quote);

    return (
        <div className={classNames(styles.result, isAi && styles.resultAi)}>
            <p className={classNames(styles.resultLabel, isAi && styles.resultLabelAi)}>
                {isAi ? "AI procjena" : "Procjena prema osnovnim podacima poziva"}
            </p>
            <p className={classNames(styles.verdict, verdict.className)}>
                <Icon name={verdict.icon} size={20}/>
                {verdict.title}
            </p>
            <p className={styles.verdictHint}>{verdict.hint}</p>

            {result.reasons.length > 0 && (
                <div className={styles.resultBlock}>
                    <h3 className={styles.resultHeading}>Zašto</h3>
                    <ul className={styles.reasonList}>
                        {result.reasons.map((reason) => (
                            <li key={`${reason.text}-${reason.quote ?? ""}`}>
                                <p>{reason.text}</p>
                                {reason.quote && (
                                    <figure className={styles.quote}>
                                        <blockquote>„{reason.quote}”</blockquote>
                                        <figcaption>iz teksta poziva</figcaption>
                                    </figure>
                                )}
                            </li>
                        ))}
                    </ul>
                </div>
            )}

            {result.missingInfo.length > 0 && (
                <div className={styles.resultBlock}>
                    <h3 className={styles.resultHeading}>Što još trebaš provjeriti</h3>
                    <ul className={styles.missingList}>
                        {result.missingInfo.map((item) => <li key={item}>{item}</li>)}
                    </ul>
                </div>
            )}

            <div className={styles.disclaimer}>
                <Icon name="info" className={styles.disclaimerIcon}/>
                <p>
                    {result.disclaimer}
                    {result.sourceUrl && (
                        <>
                            {" "}
                            <ExternalLink href={result.sourceUrl}>Službena objava natječaja</ExternalLink>
                        </>
                    )}
                </p>
            </div>
        </div>
    );
};
