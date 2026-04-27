import { FC, ReactNode } from "react";
import {
    Avatar,
    IconButton,
    ListItem,
    ListItemAvatar,
    ListItemButton,
    ListItemText,
    Tooltip,
    Typography,
} from "@mui/material";
import { DoneAll } from "@mui/icons-material";
import { formatDate, formatRelative } from "@/shared/utils/date";

interface NotificationItemLayoutProps {
    isRead: boolean;
    icon: ReactNode;
    iconColor: string;
    iconBgColor: string;
    primary: ReactNode;
    secondary: ReactNode;
    createdAt: string;
    onClick: () => void;
    onMarkAsRead?: () => void;
}

export const NotificationItemLayout: FC<NotificationItemLayoutProps> = ({
    isRead,
    icon,
    iconColor,
    iconBgColor,
    primary,
    secondary,
    createdAt,
    onClick,
    onMarkAsRead,
}) => (
    <ListItem
        disablePadding
        secondaryAction={
            !isRead && onMarkAsRead ? (
                <Tooltip title="Mark as read">
                    <IconButton
                        size="small"
                        edge="end"
                        aria-label="Mark as read"
                        onClick={(e) => {
                            e.stopPropagation();
                            onMarkAsRead();
                        }}
                        sx={{
                            opacity: 0,
                            transition: "opacity 0.15s",
                            ".MuiListItem-root:hover &": { opacity: 1 },
                        }}
                    >
                        <DoneAll fontSize="small" />
                    </IconButton>
                </Tooltip>
            ) : undefined
        }
    >
        <ListItemButton
            onClick={onClick}
            sx={{
                borderLeft: isRead ? "3px solid transparent" : 3,
                borderColor: isRead ? "transparent" : "primary.main",
                bgcolor: isRead ? "background.default" : "background.paper",
                py: 1.5,
                px: 2,
            }}
        >
            <ListItemAvatar>
                <Avatar sx={{ bgcolor: iconBgColor, color: iconColor }}>{icon}</Avatar>
            </ListItemAvatar>
            <ListItemText
                primary={primary}
                slotProps={{
                    primary: {
                        fontWeight: isRead ? "normal" : "bold",
                        variant: "body2",
                        component: "div",
                    },
                }}
                secondary={
                    <>
                        {secondary}
                        <Tooltip title={formatDate(createdAt)} placement="bottom-start">
                            <Typography
                                component="span"
                                variant="caption"
                                color="text.secondary"
                                sx={{ display: "inline-block", mt: 0.5 }}
                            >
                                {formatRelative(createdAt)}
                            </Typography>
                        </Tooltip>
                    </>
                }
            />
        </ListItemButton>
    </ListItem>
);
