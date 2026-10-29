import {ButtonLink, EmptyState, ErrorState, FundingCallCard, ListSkeleton} from "@/components";
import {useRecommendations} from "@/hooks";
import {routes} from "@/routes";
import type {Profile} from "@/types";
import {MatchDetails} from "./MatchDetails.tsx";
import {RecommendationsSummary} from "./RecommendationsSummary.tsx";
import styles from "./RecommendationsPage.module.css";

interface RecommendationsResultsProps {
    profile: Profile;
}

export const RecommendationsResults = ({profile}: RecommendationsResultsProps) => {
    const {data: results, isPending, isError, error, refetch} = useRecommendations(profile);

    if (isPending) {
        return <ListSkeleton label="Tražimo natječaje za tvoj profil..."/>;
    }
    if (isError) {
        return <ErrorState message={error.message} onRetry={() => refetch()}/>;
    }
    if (results.length === 0) {
        return (
            <EmptyState
                icon="search"
                title="Nema natječaja za ovaj profil"
                hint="Probaj dodati još područja, maknuti županiju ili upisati drukčije ključne riječi."
                actions={<ButtonLink href={routes.HOME}>Promijeni profil</ButtonLink>}
            />
        );
    }

    return (
        <>
            <RecommendationsSummary profile={profile} count={results.length}/>
            <ol className={styles.list}>
                {results.map(({fundingCall, reasons, statusUnknown}) => (
                    <li key={fundingCall.id}>
                        <FundingCallCard fundingCall={fundingCall}>
                            <MatchDetails reasons={reasons} statusUnknown={statusUnknown}/>
                        </FundingCallCard>
                    </li>
                ))}
            </ol>
        </>
    );
};
