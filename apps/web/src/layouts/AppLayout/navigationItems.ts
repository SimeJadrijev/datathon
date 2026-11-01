import {routes} from "@/routes";

interface NavigationItem {
    path: string;
    label: string;
}

export const navigationItems: NavigationItem[] = [
    {path: routes.HOME, label: "Početna"},
    {path: routes.RECOMMENDATIONS, label: "Moji natječaji"},
    {path: routes.FUNDING_CALLS, label: "Svi natječaji"},
    {path: routes.REGULATIONS, label: "Zakoni"},
    {path: routes.SOURCES, label: "Izvori"},
];

export const isNavigationItemActive = (location: string, path: string) => {
    if (path === routes.HOME) {
        return location === path;
    }
    return location === path || location.startsWith(`${path}/`);
};
