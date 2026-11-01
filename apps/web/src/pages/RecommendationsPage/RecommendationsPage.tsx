import {ButtonLink, EmptyState, PageHeader} from "@/components";
import {useProfile} from "@/hooks";
import {routes} from "@/routes";
import {RecommendationsResults} from "./RecommendationsResults.tsx";

export const RecommendationsPage = () => {
    const {profile} = useProfile();

    if (!profile) {
        return (
            <>
                <PageHeader title="Moji natječaji"/>
                <EmptyState
                    title="Najprije nam reci tko si"
                    hint="Ispuni kratak profil (manje od minute) i pokazat ćemo natječaje koji ti odgovaraju."
                    actions={<ButtonLink href={routes.HOME}>Ispuni profil</ButtonLink>}
                />
            </>
        );
    }

    return (
        <>
            <PageHeader
                title="Moji natječaji"
                description="Natječaji koji odgovaraju tvom profilu, poredani po tome koliko ti odgovaraju."
                actions={<ButtonLink href={routes.HOME} variant="secondary">Promijeni profil</ButtonLink>}
            />
            <RecommendationsResults profile={profile}/>
        </>
    );
};
