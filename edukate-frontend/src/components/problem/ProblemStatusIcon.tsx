import { ProblemStatus } from "../../types/ProblemMetadata";
import { Box, Tooltip } from "@mui/material";
import DoneIcon from "@mui/icons-material/DoneOutlined";
import CloseIcon from "@mui/icons-material/CloseOutlined";
import PendingIcon from "@mui/icons-material/PendingOutlined";
import { defaultTooltipSlotProps } from "../../utils/utils";

interface ProblemStatusIconProps {
    status?: ProblemStatus;
}

export function ProblemStatusIcon({ status }: ProblemStatusIconProps) {
    return (
        <Box justifyContent={"left"} alignContent={"center"} display={"flex"}>
            { status == "SOLVED" &&
                <Tooltip title={"Solved"} slotProps={defaultTooltipSlotProps}>
                    <DoneIcon color="success"/>
                </Tooltip>
            }
            { status == "FAILED" &&
                <Tooltip title={"Failed"} slotProps={defaultTooltipSlotProps}>
                    <CloseIcon color="error"/>
                </Tooltip>
            }
            { status == "SOLVING" &&
                <Tooltip title={"Attempted"} slotProps={defaultTooltipSlotProps}>
                    <PendingIcon color="warning"/>
                </Tooltip>
            }
        </Box>
    );
}
