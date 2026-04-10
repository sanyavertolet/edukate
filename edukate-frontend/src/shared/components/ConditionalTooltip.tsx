import { FC, ReactElement } from "react";
import { defaultTooltipSlotProps } from "@/shared/utils/utils";
import { Tooltip } from "@mui/material";

interface ConditionalTooltipProps {
    slotProps?: typeof defaultTooltipSlotProps;
    children: ReactElement;
    title: string;
    shown?: boolean;
    placement?:
        | "bottom-end"
        | "bottom-start"
        | "bottom"
        | "left-end"
        | "left-start"
        | "left"
        | "right-end"
        | "right-start"
        | "right"
        | "top-end"
        | "top-start"
        | "top";
}

export const ConditionalTooltip: FC<ConditionalTooltipProps> = ({
    slotProps = defaultTooltipSlotProps,
    children,
    title,
    shown = true,
    placement = "bottom",
}) => {
    return (
        <Tooltip slotProps={slotProps} title={shown ? title : ""} placement={placement}>
            {children}
        </Tooltip>
    );
};
