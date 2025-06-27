import { FC, ReactElement } from "react";
import { defaultTooltipSlotProps } from "../../utils/utils";
import { SlotProps, Tooltip } from "@mui/material";

interface ConditionalTooltipProps {
    slotProps?: SlotProps<never, never, never>;
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    children: ReactElement<unknown, any>;
    title: string;
    shown?: boolean;
    placement?: | 'bottom-end'
        | 'bottom-start'
        | 'bottom'
        | 'left-end'
        | 'left-start'
        | 'left'
        | 'right-end'
        | 'right-start'
        | 'right'
        | 'top-end'
        | 'top-start'
        | 'top';
}

export const ConditionalTooltip: FC<ConditionalTooltipProps> = (
    {slotProps = defaultTooltipSlotProps, children, title, shown = true, placement = "bottom"}
) => {
    return (
        <Tooltip slotProps={slotProps} title={shown ? title : ""} placement={placement}>
            {children}
        </Tooltip>
    )
};
