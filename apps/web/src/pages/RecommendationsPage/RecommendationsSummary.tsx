import {Badge} from "@/components";
import {AREA_LABELS, formatCounty, TARGET_GROUP_LABELS} from "@/constants";
import type {Profile} from "@/types";
import styles from "./RecommendationsPage.module.css";

// API-6 returns at most 20 results.
const MAX_RESULTS = 20;

const formatCount = (count: number) => {
    const isSingular = count % 10 === 1 && count % 100 !== 11;
    return isSingular ? `${count} natječaj` : `${count} natječaja`;
};

interface RecommendationsSummaryProps {
    profile: Profile;
    count: number;
}

export const RecommendationsSummary = ({profile, count}: RecommendationsSummaryProps) => {
    return (
        <section className={styles.summary} aria-label="Sažetak pretrage">
            <p className={styles.summaryText}>
                Pronašli smo <strong>{formatCount(count)}</strong> za profil{" "}
                <strong>{TARGET_GROUP_LABELS[profile.type]}</strong>
                {count >= MAX_RESULTS && ". Prikazujemo one koji ti najviše odgovaraju"}.
            </p>
            <div className={styles.profileBadges}>
                {profile.areas.map((area) => <Badge key={area} tone="primary">{AREA_LABELS[area]}</Badge>)}
                <Badge>{profile.county ? formatCounty(profile.county) : "Sve županije"}</Badge>
                {profile.keywords?.map((keyword) => <Badge key={keyword}>„{keyword}”</Badge>)}
            </div>
        </section>
    );
};
