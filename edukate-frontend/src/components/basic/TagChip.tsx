import { FC } from "react";
import { Chip, Tooltip } from "@mui/material";
import { defaultTooltipSlotProps } from "../../utils/utils";

interface TagChipProps {
    label: string;
    size?: "small" | "medium";
    variant?: "outlined" | "filled";
}

export const TagChip: FC<TagChipProps> = ({label, variant = "outlined", size = "small"}) => {
    return (
        <Tooltip title={label} slotProps={defaultTooltipSlotProps}>
            <Chip
                key={`$tag-${label}`} label={label} size={size} variant={variant}
                sx={{ textOverflow: "ellipsis", maxWidth: { "xs": 150, "sm": 150, "md": 300 } }}
            />
        </Tooltip>
    );
};
