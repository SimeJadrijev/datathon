export interface ReturnState {
    returnTo: string;
}

export const createReturnState = (returnTo: string): ReturnState => ({returnTo});

export const isReturnState = (value: unknown): value is ReturnState =>
    typeof value === "object"
    && value !== null
    && "returnTo" in value
    && typeof value.returnTo === "string"
    && value.returnTo.startsWith("/");
