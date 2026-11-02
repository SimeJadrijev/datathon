// A value of undefined (or an empty string) removes the parameter.
export type SearchParamPatch = Record<string, string | undefined>;

export const patchSearchParams = (current: URLSearchParams, patch: SearchParamPatch) => {
    const next = new URLSearchParams(current);
    for (const [name, value] of Object.entries(patch)) {
        if (value) {
            next.set(name, value);
        } else {
            next.delete(name);
        }
    }
    return next;
};

// Reads a parameter only when it is a known value, so a hand-edited URL never reaches the API as garbage.
export const readSearchParam = <T extends string>(
    params: URLSearchParams,
    name: string,
    isValid: (value: unknown) => value is T,
) => {
    const value = params.get(name);
    return isValid(value) ? value : undefined;
};

export const readTextParam = (params: URLSearchParams, name: string, maxLength: number) =>
    params.get(name)?.trim().slice(0, maxLength) || undefined;

// The URL counts pages from 1 ("?page=2" is the second page), the API from 0.
export const readPageParam = (params: URLSearchParams) => {
    const page = Number(params.get("page"));
    return Number.isInteger(page) && page > 1 ? page - 1 : 0;
};

export const toPageParam = (page: number) => (page > 0 ? String(page + 1) : undefined);

export const toHref = (path: string, params: URLSearchParams) => {
    const query = params.toString();
    return query ? `${path}?${query}` : path;
};
