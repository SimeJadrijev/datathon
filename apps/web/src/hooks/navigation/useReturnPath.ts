import {useHistoryState} from "wouter/use-browser-location";
import {isReturnState} from "./returnState.ts";

// Undefined when the page was opened directly (a shared link, a new tab).
export const useReturnPath = () => {
    const state: unknown = useHistoryState();
    return isReturnState(state) ? state.returnTo : undefined;
};
