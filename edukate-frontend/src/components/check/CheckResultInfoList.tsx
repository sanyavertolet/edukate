import { FC, ReactNode } from "react";
import { Avatar, List, ListItem, ListItemAvatar, ListItemText, Tooltip } from "@mui/material";
import { CheckResultInfo } from "../../types/check/CheckResultInfo";
import DoneIcon from "@mui/icons-material/DoneOutlined";
import ErrorIcon from "@mui/icons-material/Error";
import InternalIcon from "@mui/icons-material/Storage";

type CheckResultInfoListProps = {
    data: CheckResultInfo[]
};

export const CheckResultInfoList: FC<CheckResultInfoListProps> = ({ data }) => {
    return (
        <List>
            { data.map(CheckResultInfoItem) }
        </List>
    );
};

function getStatusVisuals(status: CheckResultInfo["status"]): { icon: ReactNode; color: string, tooltip?: string } {
    switch (status) {
        case "SUCCESS":
            return { icon: <DoneIcon />, color: "success.main", tooltip: "Solution works good" };
        case "MISTAKE":
            return { icon: <ErrorIcon />, color: "error.main", tooltip: "Solution contains a mistake" };
        case "INTERNAL_ERROR":
            return { icon: <InternalIcon />, color: "error.main", tooltip: "Server checking error" };
        default:
            return { icon: <ErrorIcon />, color: "error.main", tooltip: "Unreachable, sorry if you are to see it" };
    }
}

function CheckResultInfoItem(resultInfo: CheckResultInfo) {
    const { icon, color, tooltip } = getStatusVisuals(resultInfo.status);
    return (
        <ListItem key={resultInfo.id} divider>
            <ListItemAvatar>
                <Tooltip title={tooltip}>
                    <Avatar sx={{ bgcolor: color }}>{icon}</Avatar>
                </Tooltip>
            </ListItemAvatar>
            <ListItemText
                primary={`Trust Level: ${resultInfo.trustLevel}`}
                secondary={new Date(resultInfo.createdAt).toLocaleString()}
            />
        </ListItem>
    )
}
