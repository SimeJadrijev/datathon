import styles from "./SourcesPage.module.css";

const METHODS = [
    {
        term: "API",
        description: "Sučelje koje izvor nudi računalima. Podatke čitamo izravno i u strukturiranom obliku, bez pogađanja: Narodne novine (ELI), Sudski registar, mjere HZZ-a.",
    },
    {
        term: "Čitanje web stranica",
        description: "Gdje izvor nema API, čitamo javne stranice natječaja kao i svaki posjetitelj: poštujemo robots.txt i šaljemo najviše jedan upit u sekundi. Rokove i iznose iz PDF-ova pomaže pročitati AI, pa takve vrijednosti posebno označavamo.",
    },
    {
        term: "Snimka stanja",
        description: "Podatke spremamo u vlastitu bazu na određeni dan. Zato aplikacija radi i kad je izvor nedostupan, a datum snimke piše u podnožju svake stranice. Natječaj koji je u međuvremenu zatvoren provjeri na izvoru.",
    },
];

// Plain-language answer to "where does this come from", for people who never heard the word API.
export const SourcesExplainer = () => {
    return (
        <section className={styles.explainer} aria-labelledby="methods-title">
            <h2 id="methods-title" className={styles.heading}>Kako dolazimo do podataka</h2>
            <dl className={styles.methods}>
                {METHODS.map(({term, description}) => (
                    <div key={term} className={styles.method}>
                        <dt className={styles.methodTerm}>{term}</dt>
                        <dd className={styles.methodDescription}>{description}</dd>
                    </div>
                ))}
            </dl>
        </section>
    );
};
