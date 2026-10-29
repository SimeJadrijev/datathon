import type {MatchResult} from "@/types";
import styles from "./MatchDetails.module.css";

// Status and close deadline are already shown as badges on the card, so the list keeps only the real match reasons.
const CARD_REASONS = /^(otvoren|najavljen|rok )/;

const isMatchReason = (reason: string) => !CARD_REASONS.test(reason);

type MatchDetailsProps = Pick<MatchResult, "reasons" | "statusUnknown">;

export const MatchDetails = ({reasons, statusUnknown}: MatchDetailsProps) => {
    const matchReasons = reasons.filter(isMatchReason);

    return (
        <>
            {matchReasons.length > 0 && (
                <div className={styles.reasons}>
                    <p className={styles.reasonsTitle}>Zašto ti odgovara</p>
                    <ul className={styles.reasonList}>
                        {matchReasons.map((reason) => <li key={reason}>{reason}</li>)}
                    </ul>
                </div>
            )}
            {statusUnknown && (
                <p className={styles.warning} role="note">
                    Status natječaja nije potvrđen, provjeri na izvoru.
                </p>
            )}
        </>
    );
};
