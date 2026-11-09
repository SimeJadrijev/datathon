import {Link} from "wouter";
import {AREA_LABELS, NEW_REGULATION_DAYS, REGULATION_TYPE_LABELS} from "@/constants";
import {createReturnState, useCurrentPath} from "@/hooks";
import {regulationDetailPath} from "@/routes";
import type {RegulationSummary} from "@/types";
import {formatDate, isWithinLastDays} from "@/utils";
import styles from "./RegulationsPage.module.css";

interface RegulationRowProps {
    regulation: RegulationSummary;
}

export const RegulationRow = ({regulation}: RegulationRowProps) => {
    const returnTo = useCurrentPath();
    const {id, title, type, gazetteNumber, publishedAt, areas, hasSummary} = regulation;
    const metaText = [type && REGULATION_TYPE_LABELS[type], gazetteNumber && `NN ${gazetteNumber}`]
        .filter(Boolean)
        .join(" · ");
    const areaText = areas.map((area) => AREA_LABELS[area]).join(", ");
    const isNew = publishedAt !== null && isWithinLastDays(publishedAt, NEW_REGULATION_DAYS);

    return (
        <article className={styles.row}>
            <p className={styles.date}>
                {publishedAt ? (
                    <>
                        <span className="visually-hidden">Objavljeno </span>
                        <time dateTime={publishedAt} className="tabular-nums">{formatDate(publishedAt)}</time>
                        {isNew && <span className={styles.newLabel}>novo</span>}
                    </>
                ) : "Datum nepoznat"}
            </p>
            <div className={styles.main}>
                <p className={styles.meta}>{metaText}</p>
                <h3 className={styles.title}>
                    <Link href={regulationDetailPath(id)} state={createReturnState(returnTo)} className={styles.titleLink}>
                        {title}
                    </Link>
                </h3>
                {(areaText || hasSummary) && (
                    <p className={styles.details}>
                        {areaText}
                        {areaText && hasSummary && " · "}
                        {hasSummary && <span className={styles.aiNote}>ima AI sažetak</span>}
                    </p>
                )}
            </div>
        </article>
    );
};
