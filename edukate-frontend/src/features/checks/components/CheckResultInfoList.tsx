import { FC, ReactNode } from "react";
import { Avatar, List, ListItem, ListItemAvatar, ListItemButton, ListItemText, Tooltip } from "@mui/material";
import { CheckResultInfo } from "@/features/checks/types";
import DoneIcon from "@mui/icons-material/DoneOutlined";
import ErrorIcon from "@mui/icons-material/Error";
import InternalIcon from "@mui/icons-material/Storage";
import { formatDate } from "@/shared/utils/date";

type CheckResultInfoListProps = {
    data: CheckResultInfo[];
    onItemClick?: (id: number) => void;
};

export const CheckResultInfoList: FC<CheckResultInfoListProps> = ({ data, onItemClick }) => {
    return (
        <List disablePadding>
            {data.map((item) => (
                <CheckResultInfoItem key={item.id} resultInfo={item} onItemClick={onItemClick} />
            ))}
        </List>
    );
};

type CheckResultInfoItemProps = {
    resultInfo: CheckResultInfo;
    onItemClick?: (id: number) => void;
};

function CheckResultInfoItem({ resultInfo, onItemClick }: CheckResultInfoItemProps) {
    const { icon, color, tooltip } = getStatusVisuals(resultInfo.status);

    const content = (
        <>
            <ListItemAvatar>
                <Tooltip title={tooltip}>
                    <Avatar sx={{ bgcolor: color }}>{icon}</Avatar>
                </Tooltip>
            </ListItemAvatar>
            <ListItemText
                primary={`Trust level: ${String(Math.round(resultInfo.trustLevel * 100))}%`}
                secondary={formatDate(resultInfo.createdAt)}
            />
        </>
    );

    if (onItemClick) {
        return (
            <ListItem disablePadding divider>
                <ListItemButton
                    onClick={() => {
                        onItemClick(resultInfo.id);
                    }}
                >
                    {content}
                </ListItemButton>
            </ListItem>
        );
    }

    return <ListItem divider>{content}</ListItem>;
}

function getStatusVisuals(status: CheckResultInfo["status"]): { icon: ReactNode; color: string; tooltip?: string } {
    switch (status) {
        case "SUCCESS":
            return { icon: <DoneIcon />, color: "success.main", tooltip: "Solution works good" };
        case "MISTAKE":
            return { icon: <ErrorIcon />, color: "error.main", tooltip: "Solution contains a mistake" };
        case "INTERNAL_ERROR":
            return { icon: <InternalIcon />, color: "error.main", tooltip: "Server checking error" };
        default:
            return { icon: <ErrorIcon />, color: "error.main", tooltip: "Unknown" };
    }
}
