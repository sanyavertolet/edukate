import { FC, ReactNode } from "react";
import { Avatar, List, ListItem, ListItemAvatar, ListItemButton, ListItemText, Tooltip } from "@mui/material";
import { CheckResultInfo, CheckResultInfoErrorType } from "@/features/checks/types";
import DoneIcon from "@mui/icons-material/DoneOutlined";
import ErrorIcon from "@mui/icons-material/Error";
import InternalIcon from "@mui/icons-material/Storage";
import HourglassEmptyIcon from "@mui/icons-material/HourglassEmpty";
import { formatDate } from "@/shared/utils/date";
import { useTranslation } from "react-i18next";

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
    const { t, i18n } = useTranslation("checks");
    const { icon, color, tooltipKey } = getStatusVisuals(resultInfo.status);
    const tooltip = t(tooltipKey);

    const content = (
        <>
            <ListItemAvatar>
                <Tooltip title={tooltip}>
                    <Avatar sx={{ bgcolor: color }}>{icon}</Avatar>
                </Tooltip>
            </ListItemAvatar>
            <ListItemText
                primary={`${t("trust_level_label")}: ${String(Math.round(resultInfo.trustLevel * 100))}%`}
                secondary={
                    resultInfo.errorType !== "NONE"
                        ? `${formatErrorType(resultInfo.errorType)} — ${formatDate(resultInfo.createdAt, { locale: i18n.language })}`
                        : formatDate(resultInfo.createdAt, { locale: i18n.language })
                }
            />
        </>
    );

    if (onItemClick && resultInfo.status !== "PENDING") {
        return (
            <ListItem disablePadding>
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

    return <ListItem>{content}</ListItem>;
}

function formatErrorType(errorType: CheckResultInfoErrorType): string {
    return errorType.charAt(0) + errorType.slice(1).toLowerCase();
}

function getStatusVisuals(status: CheckResultInfo["status"]): { icon: ReactNode; color: string; tooltipKey: string } {
    switch (status) {
        case "SUCCESS":
            return { icon: <DoneIcon />, color: "success.main", tooltipKey: "correct_tooltip" };
        case "MISTAKE":
            return { icon: <ErrorIcon />, color: "error.main", tooltipKey: "mistake_tooltip" };
        case "INTERNAL_ERROR":
            return { icon: <InternalIcon />, color: "error.main", tooltipKey: "checker_error_tooltip" };
        case "PENDING":
            return { icon: <HourglassEmptyIcon />, color: "grey.500", tooltipKey: "requested_tooltip" };
        default:
            return { icon: <ErrorIcon />, color: "error.main", tooltipKey: "unknown_tooltip" };
    }
}
