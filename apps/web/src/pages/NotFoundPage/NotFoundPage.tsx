import {ButtonLink, EmptyState} from "@/components";
import {routes} from "@/routes";

export const NotFoundPage = () => {
    return (
        <EmptyState
            icon="alert"
            title="Stranica ne postoji"
            hint="Poveznica je možda zastarjela ili je adresa krivo upisana."
            actions={<ButtonLink href={routes.HOME}>Natrag na početnu</ButtonLink>}
        />
    );
};
