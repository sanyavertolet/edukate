import { FC, ReactNode, useLayoutEffect, useRef, useState } from "react";
import {
    Avatar,
    Box,
    IconButton,
    ListItem,
    ListItemAvatar,
    ListItemButton,
    ListItemText,
    Tooltip,
    Typography,
} from "@mui/material";
import { DoneAll } from "@mui/icons-material";
import { useTranslation } from "react-i18next";
import { formatDate, formatRelative } from "@/shared/utils/date";

const COLLAPSED_MAX_HEIGHT = 60;

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
}) => {
    const { i18n, t } = useTranslation("common");
    const [isExpanded, setIsExpanded] = useState(false);
    const [isOverflowing, setIsOverflowing] = useState(false);
    const contentRef = useRef<HTMLDivElement>(null);

    useLayoutEffect(() => {
        if (contentRef.current) {
            setIsOverflowing(contentRef.current.scrollHeight > COLLAPSED_MAX_HEIGHT);
        }
    }, []);

    return (
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
                            sx={{ color: "text.secondary" }}
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
                    bgcolor: "transparent",
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
                            fontWeight: isRead ? 500 : 700,
                            component: "div",
                        },
                        secondary: { component: "div" },
                    }}
                    secondary={
                        <>
                            <Box
                                ref={contentRef}
                                sx={{
                                    overflow: "hidden",
                                    maxHeight: isExpanded ? "none" : COLLAPSED_MAX_HEIGHT,
                                }}
                            >
                                {secondary}
                            </Box>
                            {isOverflowing && (
                                <Typography
                                    component="span"
                                    variant="caption"
                                    color="primary"
                                    onClick={(e) => {
                                        e.stopPropagation();
                                        setIsExpanded((prev) => !prev);
                                    }}
                                    sx={{ cursor: "pointer", display: "block", mt: 0.5 }}
                                >
                                    {isExpanded ? t("notification_show_less") : t("notification_show_more")}
                                </Typography>
                            )}
                            <Tooltip title={formatDate(createdAt, { locale: i18n.language })} placement="bottom-start">
                                <Typography
                                    component="span"
                                    variant="caption"
                                    color="text.secondary"
                                    sx={{ display: "block", mt: 0.5 }}
                                >
                                    {formatRelative(createdAt, i18n.language)}
                                </Typography>
                            </Tooltip>
                        </>
                    }
                />
            </ListItemButton>
        </ListItem>
    );
};
