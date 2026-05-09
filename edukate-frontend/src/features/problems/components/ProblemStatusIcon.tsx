import { ProblemStatus } from "@/features/problems/types";
import { Box, Tooltip } from "@mui/material";
import CheckCircleOutlineIcon from "@mui/icons-material/CheckCircleOutline";
import CancelOutlinedIcon from "@mui/icons-material/CancelOutlined";
import HourglassEmptyIcon from "@mui/icons-material/HourglassEmpty";
import { defaultTooltipSlotProps } from "@/shared/utils/utils";
import { useTranslation } from "react-i18next";

interface ProblemStatusIconProps {
    status?: ProblemStatus;
}

export function ProblemStatusIcon({ status }: ProblemStatusIconProps) {
    const { t } = useTranslation("problems");
    return (
        <Box justifyContent={"left"} alignContent={"center"} display={"flex"}>
            {status == "SOLVED" && (
                <Tooltip title={t("status_icon_solved")} slotProps={defaultTooltipSlotProps}>
                    <CheckCircleOutlineIcon color="success" />
                </Tooltip>
            )}
            {status == "FAILED" && (
                <Tooltip title={t("status_icon_failed")} slotProps={defaultTooltipSlotProps}>
                    <CancelOutlinedIcon color="error" />
                </Tooltip>
            )}
            {status == "SOLVING" && (
                <Tooltip title={t("status_icon_pending")} slotProps={defaultTooltipSlotProps}>
                    <HourglassEmptyIcon color="warning" />
                </Tooltip>
            )}
        </Box>
    );
}
