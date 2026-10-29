// Mirrors PageResponse from the API (our own page shape, not Spring's Page).
export type Page<T> = {
    content: T[];
    totalElements: number;
    page: number;
    size: number;
};
