export type Page<T> = {
    content: T[];
    totalElements: number;
    page: number;
    size: number;
};
