// Mirrors PageResponse from the API: page numbers start at 0.
export type Page<T> = {
    content: T[];
    totalElements: number;
    page: number;
    size: number;
};
