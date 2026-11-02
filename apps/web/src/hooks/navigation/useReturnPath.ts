import {useLocation, useSearch} from "wouter";
import {useHistoryState} from "wouter/use-browser-location";

interface ReturnState {
    returnTo: string;
}

const isReturnState = (value: unknown): value is ReturnState =>
    typeof value === "object"
    && value !== null
    && "returnTo" in value
    && typeof value.returnTo === "string"
    && value.returnTo.startsWith("/");

// Path with its query string, saved by list links so a detail page can lead back to the same filtered list.
export const useCurrentPath = () => {
    const [location] = useLocation();
    const search = useSearch();
    return search ? `${location}?${search}` : location;
};

export const createReturnState = (returnTo: string): ReturnState => ({returnTo});

// Undefined when the page was opened directly (a shared link, a new tab).
export const useReturnPath = () => {
    const state: unknown = useHistoryState();
    return isReturnState(state) ? state.returnTo : undefined;
};
