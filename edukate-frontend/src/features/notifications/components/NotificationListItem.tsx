import { FC, ReactElement } from "react";
import { Typography } from "@mui/material";
import { useTheme } from "@mui/material/styles";
import { alpha } from "@mui/material/styles";
import { InfoOutlined, GroupAdd, CheckCircle, Cancel, HourglassEmpty, ErrorOutline } from "@mui/icons-material";
import { NotificationItemLayout } from "./NotificationItemLayout";
import type {
    BaseNotification,
    SimpleNotification,
    CheckedNotification,
    InviteNotification,
} from "@/features/notifications/types";
import type { CheckedNotificationDtoStatus } from "@/generated/notifier";

interface StatusConfig {
    icon: ReactElement;
    paletteKey: "success" | "error" | "warning" | "action";
    label: string;
}

const checkedStatusConfig: Record<CheckedNotificationDtoStatus, StatusConfig> = {
    SUCCESS: { icon: <CheckCircle />, paletteKey: "success", label: "Passed" },
    MISTAKE: { icon: <Cancel />, paletteKey: "error", label: "Incorrect" },
    PENDING: { icon: <HourglassEmpty />, paletteKey: "warning", label: "Pending" },
    INTERNAL_ERROR: { icon: <ErrorOutline />, paletteKey: "action", label: "Error" },
};

const AVATAR_BG_ALPHA = 0.12;

interface NotificationListItemProps {
    notification: BaseNotification;
    onClick: (notification: BaseNotification) => void;
    onMarkAsRead?: (notification: BaseNotification) => void;
}

export const NotificationListItem: FC<NotificationListItemProps> = ({ notification, onClick, onMarkAsRead }) => {
    const theme = useTheme();

    const resolveColors = (paletteKey: string) => {
        const color = paletteKey === "action" ? theme.palette.text.secondary : theme.palette[paletteKey as "success"].main;
        return { iconColor: color, iconBgColor: alpha(color, AVATAR_BG_ALPHA) };
    };

    const getContent = () => {
        switch (notification._type) {
            case "simple": {
                const n = notification as SimpleNotification;
                const { iconColor, iconBgColor } = resolveColors("primary");
                return {
                    icon: <InfoOutlined />,
                    iconColor,
                    iconBgColor,
                    primary: n.title,
                    secondary: (
                        <Typography component="span" variant="body2" color="text.secondary">
                            {n.message}
                            {" from "}
                            <Typography component="span" variant="body2" color="text.primary" fontWeight="medium">
                                {n.source}
                            </Typography>
                        </Typography>
                    ),
                };
            }
            case "checked": {
                const n = notification as CheckedNotification;
                const config = checkedStatusConfig[n.status];
                const { iconColor, iconBgColor } = resolveColors(config.paletteKey);
                return {
                    icon: config.icon,
                    iconColor,
                    iconBgColor,
                    primary: `Submission ${config.label}`,
                    secondary: (
                        <Typography component="span" variant="body2" color="text.secondary">
                            Problem {n.problemKey}
                        </Typography>
                    ),
                };
            }
            case "invite": {
                const n = notification as InviteNotification;
                const { iconColor, iconBgColor } = resolveColors("secondary");
                return {
                    icon: <GroupAdd />,
                    iconColor,
                    iconBgColor,
                    primary: `${n.inviterName} invites you!`,
                    secondary: (
                        <Typography component="span" variant="body2" color="text.secondary">
                            Join problem set{" "}
                            <Typography component="span" variant="body2" color="text.primary" fontWeight="medium">
                                {n.problemSetName}
                            </Typography>
                        </Typography>
                    ),
                };
            }
            default: {
                const { iconColor, iconBgColor } = resolveColors("primary");
                return {
                    icon: <InfoOutlined />,
                    iconColor,
                    iconBgColor,
                    primary: "Notification",
                    secondary: null,
                };
            }
        }
    };

    const { icon, iconColor, iconBgColor, primary, secondary } = getContent();
    return (
        <NotificationItemLayout
            isRead={notification.isRead}
            icon={icon}
            iconColor={iconColor}
            iconBgColor={iconBgColor}
            primary={primary}
            secondary={secondary}
            createdAt={notification.createdAt}
            onClick={() => {
                onClick(notification);
            }}
            onMarkAsRead={
                onMarkAsRead
                    ? () => {
                          onMarkAsRead(notification);
                      }
                    : undefined
            }
        />
    );
};
