import {Badge, Button, ButtonLink, Card} from "@/components";
import {AREA_LABELS, formatCounty, TARGET_GROUP_LABELS} from "@/constants";
import {routes} from "@/routes";
import type {Profile} from "@/types";
import styles from "./ProfileSummary.module.css";

interface ProfileSummaryProps {
    profile: Profile;
    onEdit: () => void;
    onClear: () => void;
}

export const ProfileSummary = ({profile, onEdit, onClear}: ProfileSummaryProps) => {
    return (
        <Card as="section" className={styles.card}>
            <h2 className={styles.title}>Tvoj profil</h2>
            <dl className={styles.details}>
                <div className={styles.row}>
                    <dt>Tko si</dt>
                    <dd>{TARGET_GROUP_LABELS[profile.type]}</dd>
                </div>
                <div className={styles.row}>
                    <dt>Područja</dt>
                    <dd className={styles.badges}>
                        {profile.areas.map((area) => <Badge key={area} tone="accent">{AREA_LABELS[area]}</Badge>)}
                    </dd>
                </div>
                <div className={styles.row}>
                    <dt>Županija</dt>
                    <dd>{profile.county ? formatCounty(profile.county) : "Sve županije"}</dd>
                </div>
                {profile.activityCodes && (
                    <div className={styles.row}>
                        <dt>Djelatnost (NKD)</dt>
                        <dd>{profile.activityCodes.join(", ")}</dd>
                    </div>
                )}
                {profile.keywords && (
                    <div className={styles.row}>
                        <dt>Ključne riječi</dt>
                        <dd>{profile.keywords.join(", ")}</dd>
                    </div>
                )}
            </dl>
            <div className={styles.actions}>
                <ButtonLink href={routes.RECOMMENDATIONS}>Prikaži moje natječaje</ButtonLink>
                <Button variant="secondary" onClick={onEdit}>Promijeni profil</Button>
                <button type="button" className={styles.clearButton} onClick={onClear}>Obriši profil</button>
            </div>
            <p className={styles.note}>Profil je spremljen samo u ovom pregledniku.</p>
        </Card>
    );
};
