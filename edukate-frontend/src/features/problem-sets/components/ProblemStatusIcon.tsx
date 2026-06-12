import { FC } from "react";
import { SvgIconProps } from "@mui/material";
import CheckCircleOutlineIcon from "@mui/icons-material/CheckCircleOutline";
import RadioButtonUncheckedIcon from "@mui/icons-material/RadioButtonUnchecked";
import HourglassEmptyIcon from "@mui/icons-material/HourglassEmpty";
import CancelOutlinedIcon from "@mui/icons-material/CancelOutlined";
import { ProblemMetadataStatus } from "@/generated/backend";

interface ProblemStatusIconProps extends Omit<SvgIconProps, "color"> {
    status: ProblemMetadataStatus;
}

export const ProblemStatusIcon: FC<ProblemStatusIconProps> = ({ status, ...iconProps }) => {
    switch (status) {
        case "SOLVED":
            return <CheckCircleOutlineIcon color="success" {...iconProps} />;
        case "FAILED":
            return <CancelOutlinedIcon color="error" {...iconProps} />;
        case "SOLVING":
            return <HourglassEmptyIcon color="warning" {...iconProps} />;
        case "NOT_SOLVED":
        default:
            return <RadioButtonUncheckedIcon color="disabled" {...iconProps} />;
    }
};
