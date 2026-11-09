import {useEffect, useState} from "react";
import {useLocation} from "wouter";
import {Button, Notice, PageHeader} from "@/components";
import {useGetMeta, useProfile} from "@/hooks";
import {routes} from "@/routes";
import type {Profile} from "@/types";
import {HomeFacts} from "./HomeFacts.tsx";
import {HowItWorks} from "./HowItWorks.tsx";
import {ProfileDeadlines} from "./ProfileDeadlines.tsx";
import {ProfileForm} from "./ProfileForm.tsx";
import {ProfileSummary} from "./ProfileSummary.tsx";
import styles from "./HomePage.module.css";

const UNDO_TIMEOUT_MS = 10_000;

export const HomePage = () => {
    const {profile, saveProfile, clearProfile} = useProfile();
    const [isEditing, setIsEditing] = useState(false);
    const [clearedProfile, setClearedProfile] = useState<Profile>();
    const [, navigate] = useLocation();
    const meta = useGetMeta();

    useEffect(() => {
        if (!clearedProfile) {
            return;
        }
        const timeoutId = window.setTimeout(() => setClearedProfile(undefined), UNDO_TIMEOUT_MS);
        return () => window.clearTimeout(timeoutId);
    }, [clearedProfile]);

    const handleSubmit = (nextProfile: Profile) => {
        saveProfile(nextProfile);
        setClearedProfile(undefined);
        navigate(routes.RECOMMENDATIONS);
    };

    const handleClear = () => {
        setClearedProfile(profile);
        clearProfile();
        setIsEditing(false);
    };

    const handleUndoClear = () => {
        if (clearedProfile) {
            saveProfile(clearedProfile);
        }
        setClearedProfile(undefined);
    };

    const renderProfile = () => {
        if (profile && !isEditing) {
            return <ProfileSummary profile={profile} onEdit={() => setIsEditing(true)} onClear={handleClear}/>;
        }
        return (
            <ProfileForm
                initialProfile={profile}
                onSubmit={handleSubmit}
                onCancel={profile ? () => setIsEditing(false) : undefined}
            />
        );
    };

    return (
        <>
            <PageHeader
                title="Natječaji i potpore za koje se možeš prijaviti"
                description="Iz službenih izvora, na jednom mjestu. Reci nam tko si i izdvojit ćemo ono što ti odgovara, uz objašnjenje zašto."
                meta={<HomeFacts meta={meta}/>}
            />
            <div className={styles.layout}>
                <div className={styles.main}>
                    <div aria-live="polite">
                        {clearedProfile && (
                            <Notice tone="info" className={styles.undo}>
                                <p className={styles.undoText}>
                                    Profil je obrisan.
                                    <Button variant="secondary" size="sm" onClick={handleUndoClear}>Vrati profil</Button>
                                </p>
                            </Notice>
                        )}
                    </div>
                    {renderProfile()}
                </div>
                <aside className={styles.aside}>
                    {profile ? <ProfileDeadlines profile={profile}/> : <HowItWorks/>}
                </aside>
            </div>
        </>
    );
};
