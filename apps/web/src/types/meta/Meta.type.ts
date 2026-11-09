export type Meta = {
    snapshotDate: string | null;
    lastIngestAt: string | null;
    counts: {
        openFundingCalls: number;
        regulations: number;
    };
};
