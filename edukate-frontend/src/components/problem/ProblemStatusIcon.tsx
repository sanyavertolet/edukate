import { ProblemStatus } from "../../types/ProblemMetadata";
import { Box, Tooltip } from "@mui/material";
import DoneIcon from "@mui/icons-material/DoneOutlined";
import CloseIcon from "@mui/icons-material/CloseOutlined";
import PendingIcon from "@mui/icons-material/PendingOutlined";

interface ProblemStatusIconProps {
    status: ProblemStatus | null;
}

export function ProblemStatusIcon({ status }: ProblemStatusIconProps) {
    const tooltipSlotProps = {
        popper: {
            modifiers: [
                { name: 'offset', options: { offset: [0, -14] } },
            ],
        },
    };
    return (
        <Box justifyContent={"left"} alignContent={"center"} display={"flex"}>
            { status == "SOLVED" &&
                <Tooltip title={"Solved"} slotProps={tooltipSlotProps}>
                    <DoneIcon color="success"/>
                </Tooltip>
            }
            { status == "FAILED" &&
                <Tooltip title={"Failed"} slotProps={tooltipSlotProps}>
                    <CloseIcon color="error"/>
                </Tooltip>
            }
            { status == "SOLVING" &&
                <Tooltip title={"Attempted"} slotProps={tooltipSlotProps}>
                    <PendingIcon color="warning"/>
                </Tooltip>
            }
        </Box>
    );
}
