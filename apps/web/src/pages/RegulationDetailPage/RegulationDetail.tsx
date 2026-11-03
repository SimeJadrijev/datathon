import {AiBlock, Badge, Card, ExternalButtonLink, ExternalLink, FactList, PageHeader, UnknownValue} from "@/components";
import {AREA_LABELS, NEW_REGULATION_DAYS, REGULATION_TYPE_LABELS} from "@/constants";
import type {RegulationDetail as RegulationDetailType} from "@/types";
import {formatDate, formatDateTime, isWithinLastDays} from "@/utils";
import {CitingFundingCalls} from "./CitingFundingCalls.tsx";
import styles from "./RegulationDetailPage.module.css";

const formatGazette = ({gazetteNumber, documentNumber}: RegulationDetailType) => {
    if (!gazetteNumber) {
        return <UnknownValue/>;
    }
    return documentNumber ? `NN ${gazetteNumber}, dokument ${documentNumber}` : `NN ${gazetteNumber}`;
};

const buildFacts = (regulation: RegulationDetailType) => [
    {label: "Vrsta", value: regulation.type ? REGULATION_TYPE_LABELS[regulation.type] : <UnknownValue/>},
    {label: "Narodne novine", value: formatGazette(regulation)},
    {label: "Objavljeno", value: regulation.publishedAt ? formatDate(regulation.publishedAt) : <UnknownValue/>},
    {label: "Doneseno", value: regulation.enactedAt ? formatDate(regulation.enactedAt) : <UnknownValue/>},
    {
        label: "Područje",
        value: regulation.areas.length > 0
            ? regulation.areas.map((area) => AREA_LABELS[area]).join(", ")
            : <UnknownValue>nije razvrstano</UnknownValue>,
    },
];

interface RegulationDetailProps {
    regulation: RegulationDetailType;
}

// Editorial order: the facts, then the official text, and only then our summary of it.
export const RegulationDetail = ({regulation}: RegulationDetailProps) => {
    const {title, type, gazetteNumber, publishedAt, summary, url, eliUrl, pdfUrl, eliMetadata, fetchedAt, citingFundingCalls} = regulation;
    const description = [type && REGULATION_TYPE_LABELS[type], gazetteNumber && `Narodne novine ${gazetteNumber}`]
        .filter(Boolean)
        .join(" · ");
    const isNew = publishedAt !== null && isWithinLastDays(publishedAt, NEW_REGULATION_DAYS);

    return (
        <article className={styles.page}>
            <PageHeader
                title={title}
                description={description || undefined}
                meta={isNew ? <Badge tone="primary">Novo, objavljeno u zadnjih 30 dana</Badge> : undefined}
            />
            <div className={styles.content}>
                <Card as="section" aria-labelledby="regulation-facts-title">
                    <h2 id="regulation-facts-title" className={styles.sectionTitle}>Osnovni podaci</h2>
                    <FactList facts={buildFacts(regulation)}/>
                </Card>

                <section className={styles.officialText} aria-labelledby="official-text-title">
                    <h2 id="official-text-title" className={styles.sectionTitle}>Službeni tekst</h2>
                    <p className={styles.muted}>Mjerodavan je samo tekst objavljen u Narodnim novinama.</p>
                    <div className={styles.links}>
                        {url && <ExternalButtonLink href={url}>Otvori u Narodnim novinama</ExternalButtonLink>}
                        {pdfUrl && <ExternalLink href={pdfUrl}>PDF</ExternalLink>}
                        {eliUrl && <ExternalLink href={eliUrl}>ELI zapis</ExternalLink>}
                    </div>
                    {!url && !pdfUrl && !eliUrl && (
                        <p className={styles.muted}>Poveznica na službeni tekst nije zabilježena. Propis potraži na narodne-novine.nn.hr.</p>
                    )}
                </section>

                <Card as="section" aria-labelledby="regulation-summary-title">
                    <h2 id="regulation-summary-title" className={styles.sectionTitle}>Sažetak</h2>
                    {summary ? (
                        <AiBlock
                            label="AI sažetak"
                            note="Sažetak je napisao AI iz službenog teksta. Nije pravni savjet: prije odluke pročitaj propis u Narodnim novinama."
                        >
                            {summary}
                        </AiBlock>
                    ) : (
                        <p className={styles.muted}>Za ovaj propis još nemamo sažetak. Pročitaj službeni tekst u Narodnim novinama.</p>
                    )}
                </Card>

                {citingFundingCalls.length > 0 && <CitingFundingCalls fundingCalls={citingFundingCalls}/>}

                <p className={styles.source}>
                    Izvor: Narodne novine{eliMetadata ? " (ELI metapodaci)" : ""}.
                    {fetchedAt && ` Podaci dohvaćeni ${formatDateTime(fetchedAt)}.`}
                </p>
            </div>
        </article>
    );
};
