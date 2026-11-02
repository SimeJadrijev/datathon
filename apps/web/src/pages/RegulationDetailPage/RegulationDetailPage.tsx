import {useParams} from "wouter";
import {ApiError} from "@/api";
import {BackLink, ButtonLink, EmptyState, ErrorState, LoadingState} from "@/components";
import {useGetRegulation, useReturnPath} from "@/hooks";
import {routes} from "@/routes";
import {RegulationDetail} from "./RegulationDetail.tsx";

const parseId = (value: string | undefined) => {
    const id = Number(value);
    return Number.isInteger(id) && id > 0 ? id : undefined;
};

const RegulationNotFound = () => (
    <EmptyState
        icon="document"
        title="Ovaj propis ne postoji"
        hint="Poveznica je možda zastarjela ili krivo prepisana. Potraži propis po nazivu na popisu."
        actions={<ButtonLink href={routes.REGULATIONS}>Svi propisi</ButtonLink>}
    />
);

interface RegulationDetailContentProps {
    id: number;
}

const RegulationDetailContent = ({id}: RegulationDetailContentProps) => {
    const {data, isPending, isError, error, refetch} = useGetRegulation(id);

    if (isPending) {
        return <LoadingState message="Učitavamo propis…"/>;
    }
    if (isError && error instanceof ApiError && error.status === 404) {
        return <RegulationNotFound/>;
    }
    if (isError) {
        return <ErrorState message={error.message} onRetry={() => refetch()}/>;
    }
    return <RegulationDetail regulation={data}/>;
};

export const RegulationDetailPage = () => {
    const params = useParams<{id: string}>();
    const id = parseId(params.id);
    const returnTo = useReturnPath();
    const backHref = returnTo?.startsWith(routes.REGULATIONS) ? returnTo : routes.REGULATIONS;

    return (
        <>
            <BackLink href={backHref}>{returnTo ? "Natrag na popis propisa" : "Svi propisi"}</BackLink>
            {id ? <RegulationDetailContent id={id}/> : <RegulationNotFound/>}
        </>
    );
};
