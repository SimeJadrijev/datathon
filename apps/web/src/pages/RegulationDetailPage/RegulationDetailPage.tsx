import {useParams} from "wouter";
import {ButtonLink, EmptyState, PageHeader} from "@/components";
import {routes} from "@/routes";

export const RegulationDetailPage = () => {
    const {id} = useParams<{id: string}>();

    return (
        <>
            <PageHeader title={`Propis #${id}`}/>
            <EmptyState
                title="Detalji propisa su u izradi"
                hint="Uskoro ćeš ovdje vidjeti sažetak propisa i poveznice na Narodne novine."
                actions={<ButtonLink href={routes.REGULATIONS} variant="secondary">Svi propisi</ButtonLink>}
            />
        </>
    );
};
