import { ReactNode } from "react";

export type AdditionalNavigationElement = {
    text: string;
    onClick: () => void;
    icon?: ReactNode;
    isSelected?: boolean;
}
