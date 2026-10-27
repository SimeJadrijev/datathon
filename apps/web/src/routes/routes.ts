export const routes = {
    HOME: "/",
    RECOMMENDATIONS: "/preporuke",
    FUNDING_CALLS: "/natjecaji",
    FUNDING_CALL_DETAIL: "/natjecaji/:id",
    REGULATIONS: "/zakoni",
    REGULATION_DETAIL: "/zakoni/:id",
    SOURCES: "/izvori",
} as const;

export const fundingCallDetailPath = (id: number) => `${routes.FUNDING_CALLS}/${id}`;

export const regulationDetailPath = (id: number) => `${routes.REGULATIONS}/${id}`;
