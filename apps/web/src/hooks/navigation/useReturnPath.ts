import {useHistoryState} from "wouter/use-browser-location";
import {isReturnState} from "./returnState.ts";

export const useReturnPath = () => {
    const state: unknown = useHistoryState();
    return isReturnState(state) ? state.returnTo : undefined;
};
