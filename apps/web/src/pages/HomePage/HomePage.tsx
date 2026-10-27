import {ButtonLink, EmptyState, PageHeader} from "@/components";
import {routes} from "@/routes";

export const HomePage = () => {
    return (
        <>
            <PageHeader
                title="Svi natječaji i novi zakoni na jednom mjestu"
                description="Natječaji, poticaji, stipendije i novi propisi iz službenih izvora, s poveznicom na izvor uz svaki podatak."
            />
            <EmptyState
                title="Profil uskoro"
                hint="Ovdje ćeš u minuti unijeti kratki profil i dobiti natječaje koji ti odgovaraju."
                actions={
                    <>
                        <ButtonLink href={routes.FUNDING_CALLS}>Pregledaj natječaje</ButtonLink>
                        <ButtonLink href={routes.REGULATIONS} variant="secondary">Novi zakoni</ButtonLink>
                    </>
                }
            />
        </>
    );
};
