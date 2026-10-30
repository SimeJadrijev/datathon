import {Link} from "wouter";
import {Button, DeadlineDate} from "@/components";
import {useGetProfileMatches} from "@/hooks";
import {fundingCallDetailPath, routes} from "@/routes";
import type {FundingCallSummary, MatchResult, Profile} from "@/types";
import {formatDeadline, getDaysUntil, isDeadlineUrgent} from "@/utils";
import styles from "./SidePanel.module.css";

const DEADLINES_LIMIT = 3;

type DatedFundingCall = FundingCallSummary & {deadline: string};

const hasUpcomingDeadline = (fundingCall: FundingCallSummary): fundingCall is DatedFundingCall =>
    fundingCall.deadline !== null && getDaysUntil(fundingCall.deadline) >= 0;

// The profile's matches whose deadline comes next. Matches without a deadline cannot be "next", so they are left out.
// ISO date strings sort correctly as text.
const getNextDeadlines = (results: MatchResult[]) =>
    results
        .map(({fundingCall}) => fundingCall)
        .filter(hasUpcomingDeadline)
        .sort((first, second) => first.deadline.localeCompare(second.deadline))
        .slice(0, DEADLINES_LIMIT);

interface ProfileDeadlinesBodyProps {
    profile: Profile;
}

const ProfileDeadlinesBody = ({profile}: ProfileDeadlinesBodyProps) => {
    const {data: results, isPending, isError, refetch} = useGetProfileMatches(profile);

    if (isPending) {
        return <p className={styles.state} role="status">Tražimo rokove za tvoj profil…</p>;
    }
    if (isError) {
        return (
            <div className={styles.state} role="alert">
                <p>Rokove trenutno ne možemo učitati.</p>
                <Button variant="secondary" size="sm" onClick={() => refetch()}>Pokušaj ponovno</Button>
            </div>
        );
    }

    const nextDeadlines = getNextDeadlines(results);
    if (nextDeadlines.length === 0) {
        return <p className={styles.state}>Za tvoj profil trenutno nema natječaja s poznatim rokom.</p>;
    }

    return (
        <ol className={styles.list}>
            {nextDeadlines.map((fundingCall) => (
                <li key={fundingCall.id} className={styles.item}>
                    <DeadlineDate deadline={fundingCall.deadline} isExtractedByAi={fundingCall.extractedByLlm} isCompact/>
                    <div className={styles.text}>
                        {isDeadlineUrgent(fundingCall.deadline) && <span className={styles.soon}>Rok uskoro</span>}
                        <Link href={fundingCallDetailPath(fundingCall.id)} className={styles.title}>
                            {fundingCall.title}
                        </Link>
                        <span className={styles.detail}>
                            {formatDeadline(fundingCall.deadline)}
                            {fundingCall.provider && ` · ${fundingCall.provider}`}
                        </span>
                    </div>
                </li>
            ))}
        </ol>
    );
};

interface ProfileDeadlinesProps {
    profile: Profile;
}

// Shown once a profile exists: the next deadlines among the profile's own matches (same query as "Moji natječaji").
export const ProfileDeadlines = ({profile}: ProfileDeadlinesProps) => {
    return (
        <section className={styles.panel} aria-labelledby="profile-deadlines-title">
            <h2 id="profile-deadlines-title" className={styles.heading}>Sljedeći rokovi za tebe</h2>
            <ProfileDeadlinesBody profile={profile}/>
            <Link href={routes.RECOMMENDATIONS} className={styles.more}>Svi natječaji za tvoj profil</Link>
        </section>
    );
};
