import {EmptyState, ErrorState, ListSkeleton, PageHeader} from "@/components";
import {useGetMeta, useGetSources} from "@/hooks";
import {formatCount, formatDate, pluralize} from "@/utils";
import {SourcesTable} from "./SourcesTable.tsx";
import styles from "./SourcesPage.module.css";

const SourcesContent = () => {
    const {data: sources, isPending, isError, error, refetch} = useGetSources();

    if (isPending) {
        return <ListSkeleton label="Učitavamo izvore…" rows={3}/>;
    }
    if (isError) {
        return <ErrorState message={error.message} onRetry={() => refetch()}/>;
    }
    if (sources.length === 0) {
        return (
            <EmptyState
                icon="database"
                title="Popis izvora je prazan"
                hint="Poslužitelj nije vratio nijedan izvor. Pokušaj ponovno za koju minutu."
            />
        );
    }
    return <SourcesTable sources={sources}/>;
};

export const SourcesPage = () => {
    const {data: meta} = useGetMeta();
    const {data: sources} = useGetSources();

    const facts = [
        sources && `${formatCount(sources.length)} ${pluralize(sources.length, {one: "izvor", few: "izvora", many: "izvora"})}`,
        meta?.snapshotDate && `stanje podataka na dan ${formatDate(meta.snapshotDate)}`,
    ].filter(Boolean);

    return (
        <>
            <PageHeader
                title="Izvori podataka"
                description="Odakle dolaze natječaji i propisi, kako ih prikupljamo i koliko su svježi. Uz svaki zapis u aplikaciji vodi poveznica na izvornu objavu."
                meta={facts.length > 0 ? <p className={styles.facts}>{facts.join(" · ")}</p> : undefined}
            />
            <section aria-labelledby="sources-title">
                <h2 id="sources-title" className={styles.heading}>Popis izvora</h2>
                <SourcesContent/>
            </section>
        </>
    );
};
