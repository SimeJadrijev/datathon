import {EmptyState, PageHeader} from "@/components";

export const FundingCallsPage = () => {
    return (
        <>
            <PageHeader
                title="Svi natječaji"
                description="Pretraži i filtriraj natječaje, poticaje i stipendije iz svih izvora."
            />
            <EmptyState
                title="Popis natječaja je u izradi"
                hint="Uskoro ćeš ovdje moći pretraživati natječaje po statusu, skupini, području i županiji."
            />
        </>
    );
};
