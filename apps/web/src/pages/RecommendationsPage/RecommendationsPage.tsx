import {ButtonLink, EmptyState, PageHeader} from "@/components";
import {routes} from "@/routes";

export const RecommendationsPage = () => {
    return (
        <>
            <PageHeader
                title="Moji natječaji"
                description="Natječaji koji odgovaraju tvom profilu i zašto ti odgovaraju."
            />
            <EmptyState
                title="Preporuke su u izradi"
                hint="Uskoro ćeš ovdje vidjeti natječaje prema svom profilu."
                actions={<ButtonLink href={routes.HOME} variant="secondary">Natrag na početnu</ButtonLink>}
            />
        </>
    );
};
