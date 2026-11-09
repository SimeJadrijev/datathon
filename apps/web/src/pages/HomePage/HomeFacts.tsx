import type {UseQueryResult} from "@tanstack/react-query";
import {Link} from "wouter";
import {routes} from "@/routes";
import type {Meta} from "@/types";
import {formatCount, formatDateTime, pluralize} from "@/utils";
import styles from "./HomePage.module.css";

const describeOpenCalls = (count: number) =>
    pluralize(count, {one: "otvoren natječaj", few: "otvorena natječaja", many: "otvorenih natječaja"});

const describeRegulations = (count: number) => pluralize(count, {one: "propis", few: "propisa", many: "propisa"});

interface HomeFactsProps {
    meta: UseQueryResult<Meta>;
}

export const HomeFacts = ({meta}: HomeFactsProps) => {
    if (!meta.data) {
        return <p className={styles.facts}><Link href={routes.SOURCES}>Podaci iz službenih izvora</Link></p>;
    }

    const {counts, lastIngestAt} = meta.data;

    return (
        <p className={styles.facts}>
            <strong className="tabular-nums">{formatCount(counts.openFundingCalls)}</strong>{" "}
            {describeOpenCalls(counts.openFundingCalls)}
            {" · "}
            <strong className="tabular-nums">{formatCount(counts.regulations)}</strong>{" "}
            {describeRegulations(counts.regulations)} iz Narodnih novina
            {" · "}
            <Link href={routes.SOURCES}>
                {lastIngestAt ? `osvježeno ${formatDateTime(lastIngestAt)}` : "izvori podataka"}
            </Link>
        </p>
    );
};
