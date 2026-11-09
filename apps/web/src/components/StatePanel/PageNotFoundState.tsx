import {ButtonLink} from "@/components/Button";
import {EmptyState} from "./EmptyState.tsx";

interface PageNotFoundStateProps {
    firstPageHref: string;
}

export const PageNotFoundState = ({firstPageHref}: PageNotFoundStateProps) => {
    return (
        <EmptyState
            icon="document"
            title="Stranica ne postoji"
            hint="Popis je kraći nego što poveznica kaže. Rezultati te čekaju na prvoj stranici."
            actions={<ButtonLink href={firstPageHref} variant="secondary">Idi na prvu stranicu</ButtonLink>}
        />
    );
};
