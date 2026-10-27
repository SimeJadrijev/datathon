import {EmptyState, PageHeader} from "@/components";

export const RegulationsPage = () => {
    return (
        <>
            <PageHeader
                title="Zakoni i propisi"
                description="Novi zakoni, uredbe i pravilnici iz Narodnih novina."
            />
            <EmptyState
                title="Popis propisa je u izradi"
                hint="Uskoro ćeš ovdje moći pretraživati propise po vrsti, razdoblju i području."
            />
        </>
    );
};
