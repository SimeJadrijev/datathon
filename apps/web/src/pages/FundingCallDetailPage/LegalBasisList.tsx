import {Link} from "wouter";
import {ExternalLink} from "@/components";
import {regulationDetailPath} from "@/routes";
import type {LegalBasis} from "@/types";
import styles from "./FundingCallDetailPage.module.css";

type CitationGroup = {
    citedTitle: string | null;
    issues: LegalBasis[];
};

const groupByCitedTitle = (legalBasis: LegalBasis[]): CitationGroup[] => {
    const groups: CitationGroup[] = [];
    for (const issue of legalBasis) {
        const group = groups.find(({citedTitle}) => citedTitle === issue.citedTitle);
        if (group) {
            group.issues.push(issue);
        } else {
            groups.push({citedTitle: issue.citedTitle, issues: [issue]});
        }
    }
    return groups;
};

interface IssueLinkProps {
    issue: LegalBasis;
}

const IssueLink = ({issue}: IssueLinkProps) => {
    const label = `NN ${issue.gazetteNumber}`;
    if (issue.regulationId === null) {
        return <ExternalLink href={issue.gazetteUrl}>{label}</ExternalLink>;
    }
    return (
        <Link href={regulationDetailPath(issue.regulationId)} title={issue.regulationTitle ?? undefined}>
            {label}
        </Link>
    );
};

interface LegalBasisListProps {
    legalBasis: LegalBasis[];
}

export const LegalBasisList = ({legalBasis}: LegalBasisListProps) => (
    <>
        <ul className={styles.legalBasisList}>
            {groupByCitedTitle(legalBasis).map(({citedTitle, issues}) => (
                <li key={`${citedTitle}-${issues[0].gazetteNumber}`}>
                    <span className={styles.citedTitle}>{citedTitle ?? "Narodne novine"}</span>
                    <span className={styles.issues}>
                        {issues.map((issue, position) => (
                            <span key={issue.gazetteNumber}>
                                {position > 0 && ", "}
                                <IssueLink issue={issue}/>
                            </span>
                        ))}
                    </span>
                </li>
            ))}
        </ul>
        <p className={styles.aiNote}>
            Propise smo prepoznali u tekstu objave. Brojevi koje imamo u bazi vode na stranicu propisa, ostali na broj Narodnih novina.
        </p>
    </>
);
