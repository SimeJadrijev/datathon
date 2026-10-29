import {AREA_LABELS, formatCounty, TARGET_GROUP_LABELS} from "@/constants";
import type {Profile} from "@/types";
import {pluralize} from "@/utils";
import styles from "./RecommendationsPage.module.css";

// API-6 returns at most 20 results.
const MAX_RESULTS = 20;

const formatResultCount = (count: number) =>
    `${count} ${pluralize(count, {one: "natječaj", few: "natječaja", many: "natječaja"})}`;

const describeProfile = (profile: Profile) => {
    const areas = profile.areas.map((area) => AREA_LABELS[area].toLowerCase());
    const place = profile.county ? formatCounty(profile.county) : "cijela Hrvatska";
    const keywords = profile.keywords?.map((keyword) => `„${keyword}”`) ?? [];
    return [...areas, place, ...keywords].join(", ");
};

interface RecommendationsSummaryProps {
    profile: Profile;
    count: number;
}

export const RecommendationsSummary = ({profile, count}: RecommendationsSummaryProps) => {
    return (
        // h2 so the result rows (h3) sit under a heading; styled as a plain summary line.
        <h2 className={styles.summary}>
            <strong>{formatResultCount(count)}</strong> za profil <strong>{TARGET_GROUP_LABELS[profile.type]}</strong>
            {" "}({describeProfile(profile)})
            {count >= MAX_RESULTS && ". Prikazujemo one koji ti najviše odgovaraju"}.
        </h2>
    );
};
