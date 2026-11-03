import {useLocation, useSearch} from "wouter";

// Path with its query string, saved by list links so a detail page can lead back to the same filtered list.
export const useCurrentPath = () => {
    const [location] = useLocation();
    const search = useSearch();
    return search ? `${location}?${search}` : location;
};
