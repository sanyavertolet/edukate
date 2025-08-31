import { Avatar } from "@mui/material";
import { ReactNode } from "react";

export type NavigationElement = {
    icon?: ReactNode;
    text: string;
    href: string;
}

export const desktopNavigationElements: NavigationElement[] = [
    { text: "Problems", href: "/problems" },
    { text: "Bundles", href: "/bundles" },
];

export const mobileNavigationElements: NavigationElement[] = [
    ...desktopNavigationElements,
];
