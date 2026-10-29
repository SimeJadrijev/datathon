import {useState} from "react";
import {useLocation} from "wouter";
import {PageHeader} from "@/components";
import {useProfile} from "@/hooks";
import {routes} from "@/routes";
import type {Profile} from "@/types";
import {BrowseLinks} from "./BrowseLinks.tsx";
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
                eyebrow="Natječaji, potpore, stipendije i propisi"
                title="Pronađi natječaje koji ti odgovaraju"
                description="Prikupljamo natječaje i nove propise iz službenih izvora. Reci nam tko si i što te zanima, a mi izdvajamo ono što ti odgovara i objašnjavamo zašto."
            />
            <div className={styles.layout}>
                {renderProfile()}
                <aside className={styles.aside} aria-label="Dodatne informacije">
                    <HowItWorks/>
                    <BrowseLinks/>
                </aside>
            </div>
        </>
    );
};
