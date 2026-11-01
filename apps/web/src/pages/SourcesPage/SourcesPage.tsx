import {EmptyState, PageHeader} from "@/components";

export const SourcesPage = () => {
    return (
        <>
            <PageHeader
                title="Izvori podataka"
                description="Odakle dolaze podaci, kako ih dohvaćamo i koliko su svježi."
            />
            <EmptyState
                icon="database"
                title="Popis izvora je u izradi"
                hint="Uskoro ćeš ovdje vidjeti sve izvore s brojem zapisa i datumom zadnjeg dohvata."
            />
        </>
    );
};
