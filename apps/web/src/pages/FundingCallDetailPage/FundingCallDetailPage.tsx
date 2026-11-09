import {useParams} from "wouter";
import {ApiError} from "@/api";
import {BackLink, ButtonLink, EmptyState, ErrorState, LoadingState} from "@/components";
import {useGetFundingCall, useReturnPath} from "@/hooks";
import {routes} from "@/routes";
import {FundingCallDetail} from "./FundingCallDetail.tsx";

const parseId = (value: string | undefined) => {
    const id = Number(value);
    return Number.isInteger(id) && id > 0 ? id : undefined;
};

const useBackLink = () => {
    const returnTo = useReturnPath();
    if (returnTo?.startsWith(routes.RECOMMENDATIONS)) {
        return {href: returnTo, label: "Natrag na moje natječaje"};
    }
    if (returnTo?.startsWith(routes.FUNDING_CALLS)) {
        return {href: returnTo, label: "Natrag na popis natječaja"};
    }
    if (returnTo === routes.HOME) {
        return {href: returnTo, label: "Natrag na početnu"};
    }
    return {href: routes.FUNDING_CALLS, label: "Svi natječaji"};
};

const FundingCallNotFound = () => (
    <EmptyState
        icon="alert"
        title="Ovaj natječaj ne postoji"
        hint="Možda je uklonjen iz izvora ili je poveznica krivo prepisana. Potraži ga među svim natječajima."
        actions={<ButtonLink href={routes.FUNDING_CALLS}>Svi natječaji</ButtonLink>}
    />
);

interface FundingCallDetailContentProps {
    id: number;
}

const FundingCallDetailContent = ({id}: FundingCallDetailContentProps) => {
    const {data, isPending, isError, error, refetch} = useGetFundingCall(id);

    if (isPending) {
        return <LoadingState message="Učitavamo natječaj…"/>;
    }
    if (isError && error instanceof ApiError && error.status === 404) {
        return <FundingCallNotFound/>;
    }
    if (isError) {
        return <ErrorState message={error.message} onRetry={() => refetch()}/>;
    }
    return <FundingCallDetail fundingCall={data}/>;
};

export const FundingCallDetailPage = () => {
    const params = useParams<{id: string}>();
    const id = parseId(params.id);
    const backLink = useBackLink();

    return (
        <>
            <BackLink href={backLink.href}>{backLink.label}</BackLink>
            {id ? <FundingCallDetailContent id={id}/> : <FundingCallNotFound/>}
        </>
    );
};
