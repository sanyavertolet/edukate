import { Avatar } from "@mui/material";
import { ReactNode } from "react";

export type NavigationElement = {
    icon?: ReactNode;
    text: string;
    href: string;
};

const homeNavigationElement: NavigationElement = {
    text: "home",
    href: "/",
    icon: <Avatar alt="Home" src="logo.png" />,
};

export const desktopNavigationElements: NavigationElement[] = [
    { text: "problems", href: "/problems" },
    { text: "submissions", href: "/submissions" },
    { text: "problem_sets", href: "/problem-sets" },
];

export const mobileNavigationElements: NavigationElement[] = [homeNavigationElement, ...desktopNavigationElements];
