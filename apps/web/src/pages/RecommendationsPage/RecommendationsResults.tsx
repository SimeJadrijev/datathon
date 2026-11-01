import {ButtonLink, EmptyState, ErrorState, FundingCallList, FundingCallRow, ListSkeleton} from "@/components";
import {useGetProfileMatches} from "@/hooks";
import {routes} from "@/routes";
import type {Profile} from "@/types";
import {MatchDetails} from "./MatchDetails.tsx";
import {RecommendationsSummary} from "./RecommendationsSummary.tsx";

interface RecommendationsResultsProps {
    profile: Profile;
}

export const RecommendationsResults = ({profile}: RecommendationsResultsProps) => {
    const {data: results, isPending, isError, error, refetch} = useGetProfileMatches(profile);

    if (isPending) {
        return <ListSkeleton label="Tražimo natječaje za tvoj profil…"/>;
    }
    if (isError) {
        return <ErrorState message={error.message} onRetry={() => refetch()}/>;
    }
    if (results.length === 0) {
        return (
            <EmptyState
                title="Nema natječaja za ovaj profil"
                hint="Probaj dodati još područja, maknuti županiju ili upisati drukčije ključne riječi."
                actions={<ButtonLink href={routes.HOME}>Promijeni profil</ButtonLink>}
            />
        );
    }

    return (
        <>
            <RecommendationsSummary profile={profile} count={results.length}/>
            <FundingCallList>
                {results.map(({fundingCall, reasons, statusUnknown}) => (
                    <li key={fundingCall.id}>
                        <FundingCallRow fundingCall={fundingCall}>
                            <MatchDetails reasons={reasons} statusUnknown={statusUnknown}/>
                        </FundingCallRow>
                    </li>
                ))}
            </FundingCallList>
        </>
    );
};
