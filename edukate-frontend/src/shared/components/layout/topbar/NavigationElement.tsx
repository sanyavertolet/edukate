import { Avatar } from "@mui/material";
import { ReactNode } from "react";

export type NavigationElement = {
    icon?: ReactNode;
    text: string;
    href: string;
};

const homeNavigationElement: NavigationElement = {
    text: "Edukate",
    href: "/",
    icon: <Avatar alt="Home" src="logo.png" />,
};

export const desktopNavigationElements: NavigationElement[] = [
    { text: "Problems", href: "/problems" },
    { text: "Problem Sets", href: "/problem-sets" },
];

export const mobileNavigationElements: NavigationElement[] = [homeNavigationElement, ...desktopNavigationElements];
