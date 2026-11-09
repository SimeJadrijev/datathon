import {useLocation, useSearch} from "wouter";

export const useCurrentPath = () => {
    const [location] = useLocation();
    const search = useSearch();
    return search ? `${location}?${search}` : location;
};
