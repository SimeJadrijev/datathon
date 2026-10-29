import {useState} from "react";
import {useLocation} from "wouter";
import {PageHeader} from "@/components";
import {useProfile} from "@/hooks";
import {routes} from "@/routes";
import type {Profile} from "@/types";
import {HowItWorks} from "./HowItWorks.tsx";
import {ProfileForm} from "./ProfileForm.tsx";
import {ProfileSummary} from "./ProfileSummary.tsx";
import styles from "./HomePage.module.css";

export const HomePage = () => {
    const {profile, saveProfile, clearProfile} = useProfile();
    const [isEditing, setIsEditing] = useState(false);
    const [, navigate] = useLocation();

    const handleSubmit = (nextProfile: Profile) => {
        saveProfile(nextProfile);
        navigate(routes.RECOMMENDATIONS);
    };

    const handleClear = () => {
        clearProfile();
        setIsEditing(false);
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
                title="Svi natječaji i novi zakoni na jednom mjestu"
                description="Natječaji, poticaji i stipendije iz službenih izvora. Reci nam tko si i što te zanima, a mi ćemo izdvojiti ono što ti odgovara."
            />
            <div className={styles.layout}>
                {renderProfile()}
                <aside className={styles.aside}>
                    <HowItWorks/>
                </aside>
            </div>
        </>
    );
};
