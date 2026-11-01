import {useParams} from "wouter";
import {ButtonLink, EmptyState, PageHeader} from "@/components";
import {routes} from "@/routes";

export const FundingCallDetailPage = () => {
    const {id} = useParams<{id: string}>();

    return (
        <>
            <PageHeader title={`Natječaj #${id}`}/>
            <EmptyState
                title="Detalji natječaja su u izradi"
                hint="Uskoro ćeš ovdje vidjeti uvjete, rokove, iznose i poveznicu na izvor."
                actions={<ButtonLink href={routes.FUNDING_CALLS} variant="secondary">Svi natječaji</ButtonLink>}
            />
        </>
    );
};
