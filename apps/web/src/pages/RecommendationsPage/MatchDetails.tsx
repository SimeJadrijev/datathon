import type {MatchResult} from "@/types";
import styles from "./MatchDetails.module.css";

const ROW_REASONS = /^(otvoren|najavljen|rok )/;

const isMatchReason = (reason: string) => !ROW_REASONS.test(reason);

type MatchDetailsProps = Pick<MatchResult, "reasons" | "statusUnknown">;

export const MatchDetails = ({reasons, statusUnknown}: MatchDetailsProps) => {
    const matchReasons = reasons.filter(isMatchReason);

    return (
        <>
            {matchReasons.length > 0 && (
                <p className={styles.reasons}>
                    <span className={styles.reasonsTitle}>Odgovara ti: </span>
                    {matchReasons.join(" · ")}
                </p>
            )}
            {statusUnknown && (
                <p className={styles.warning}>Status nije potvrđen, provjeri ga na izvoru.</p>
            )}
        </>
    );
};
