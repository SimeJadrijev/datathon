import type {MatchResult} from "@/types";
import styles from "./MatchDetails.module.css";

type MatchDetailsProps = Pick<MatchResult, "reasons" | "statusUnknown">;

export const MatchDetails = ({reasons, statusUnknown}: MatchDetailsProps) => {
    return (
        <>
            {reasons.length > 0 && (
                <div className={styles.reasons}>
                    <p className={styles.reasonsTitle}>Zašto ti odgovara</p>
                    <ul className={styles.reasonList}>
                        {reasons.map((reason) => <li key={reason}>{reason}</li>)}
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
