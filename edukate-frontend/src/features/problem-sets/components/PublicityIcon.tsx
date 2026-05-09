import PublicIcon from "@mui/icons-material/Public";
import LockIcon from "@mui/icons-material/Lock";
import Tooltip from "@mui/material/Tooltip";
import { defaultTooltipSlotProps } from "@/shared/utils/utils";
import { useTranslation } from "react-i18next";

interface PublicityIconProps {
    isPublic: boolean;
    disableTooltip?: boolean;
}

export function PublicityIcon({ isPublic, disableTooltip }: PublicityIconProps) {
    const { t } = useTranslation("problem-sets");
    const icon = isPublic ? <PublicIcon color={"success"} /> : <LockIcon color={"warning"} />;
    if (disableTooltip) {
        return icon;
    }
    return (
        <Tooltip
            slotProps={defaultTooltipSlotProps}
            title={isPublic ? t("public_problem_set_tooltip") : t("private_problem_set_tooltip")}
        >
            {icon}
        </Tooltip>
    );
}
