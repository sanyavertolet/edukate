import { ProblemStatus } from "@/features/problems/types";
import { Box, Tooltip } from "@mui/material";
import CheckCircleOutlineIcon from "@mui/icons-material/CheckCircleOutline";
import CancelOutlinedIcon from "@mui/icons-material/CancelOutlined";
import HourglassEmptyIcon from "@mui/icons-material/HourglassEmpty";
import { defaultTooltipSlotProps } from "@/shared/utils/utils";

interface ProblemStatusIconProps {
    status?: ProblemStatus;
}

export function ProblemStatusIcon({ status }: ProblemStatusIconProps) {
    return (
        <Box justifyContent={"left"} alignContent={"center"} display={"flex"}>
            {status == "SOLVED" && (
                <Tooltip title={"Solved"} slotProps={defaultTooltipSlotProps}>
                    <CheckCircleOutlineIcon color="success" />
                </Tooltip>
            )}
            {status == "FAILED" && (
                <Tooltip title={"Failed"} slotProps={defaultTooltipSlotProps}>
                    <CancelOutlinedIcon color="error" />
                </Tooltip>
            )}
            {status == "SOLVING" && (
                <Tooltip title={"Pending review"} slotProps={defaultTooltipSlotProps}>
                    <HourglassEmptyIcon color="warning" />
                </Tooltip>
            )}
        </Box>
    );
}
