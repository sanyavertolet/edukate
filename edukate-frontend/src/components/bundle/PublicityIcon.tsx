import PublicIcon from "@mui/icons-material/Public";
import LockIcon from "@mui/icons-material/Lock";
import Tooltip from "@mui/material/Tooltip";

interface PublicityIconProps {
    isPublic: boolean;
    disableTooltip?: boolean;
}

export function PublicityIcon({isPublic, disableTooltip}: PublicityIconProps) {
    const icon = isPublic ? <PublicIcon color={"success"}/> : <LockIcon color={"warning"} />;
    if (disableTooltip) {
        return icon;
    }
    return (
        <Tooltip title={isPublic ? "Public bundle" : "Private bundle"}>
            { icon }
        </Tooltip>
    );
}
