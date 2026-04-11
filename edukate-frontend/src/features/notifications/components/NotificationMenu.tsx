import { ChangeEvent, FC, useMemo, useState } from "react";
import { useGetNotificationsRequest, useMarkAllNotificationsAsReadMutation } from "@/features/notifications/api";
import { Box, Button, Divider, Menu, MenuItem, Pagination, Typography } from "@mui/material";
import { MarkEmailRead } from "@mui/icons-material";
import {
    NotificationStatistics,
    BaseNotification,
    SimpleNotification,
    InviteNotification,
    CheckedNotification,
} from "@/features/notifications/types";
import { SimpleNotificationComponent } from "./SimpleNotification";
import { InviteNotificationComponent } from "./InviteNotification";
import { CheckedNotificationComponent } from "./CheckedNotification";

interface NotificationMenuProps {
    anchorEl?: HTMLElement;
    onClose: () => void;
    onNotificationClick: (notification: BaseNotification) => void;
}

const defaultNotificationPageSize: number = 10;

const notificationsMenuPrefixSx = {
    p: 1,
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
} as const;

function calculateNumberOfPage(statistics: NotificationStatistics | undefined | null) {
    return statistics ? Math.ceil(statistics.total / defaultNotificationPageSize) : 1;
}

export const NotificationMenu: FC<NotificationMenuProps> = ({ anchorEl, onClose, onNotificationClick }) => {
    const [page, setPage] = useState(1);
    const { data: notificationPage, refetch: refetchNotifications } = useGetNotificationsRequest(
        undefined,
        defaultNotificationPageSize,
        page - 1,
    );
    const notifications = notificationPage?.notifications;
    const stats = notificationPage?.statistics;

    const markAllAsReadMutation = useMarkAllNotificationsAsReadMutation();
    const handleMarkAllAsRead = () => {
        markAllAsReadMutation.mutate();
        void refetchNotifications();
    };

    const onPaginationChange = (_: ChangeEvent<unknown>, newPage: number) => {
        setPage(newPage);
    };
    const isMenuOpen = Boolean(anchorEl);
    const numberOfPages = useMemo(() => calculateNumberOfPage(stats), [stats]);
    return (
        <Menu
            id="notifications-menu"
            open={isMenuOpen}
            onClose={onClose}
            anchorEl={anchorEl}
            anchorOrigin={{ vertical: "bottom", horizontal: "right" }}
            transformOrigin={{ vertical: "top", horizontal: "right" }}
            MenuListProps={{ "aria-labelledby": "notifications-button" }}
            slotProps={{ paper: { style: { maxHeight: "400px", width: "350px" } } }}
        >
            <Box key="notifications-menu-header" sx={notificationsMenuPrefixSx}>
                <Typography variant="subtitle2" fontWeight="bold">
                    Notifications
                </Typography>
                <Button size="small" startIcon={<MarkEmailRead />} onClick={handleMarkAllAsRead}>
                    Mark all as read
                </Button>
            </Box>
            <Divider />
            {notifications &&
                notifications.map((notification) => (
                    <NotificationItem
                        key={notification.uuid}
                        notification={notification}
                        onClick={() => {
                            onNotificationClick(notification);
                        }}
                    />
                ))}
            {notifications && numberOfPages > 1 && (
                <Box key="notifications-menu-footer">
                    <Divider sx={{ my: 1 }} />
                    <Pagination
                        count={numberOfPages}
                        page={page}
                        showFirstButton
                        showLastButton
                        onChange={onPaginationChange}
                        size={"small"}
                        color={"primary"}
                        sx={{ justifySelf: "center" }}
                    />
                </Box>
            )}
        </Menu>
    );
};

interface NotificationItemProps {
    notification: BaseNotification;
    onClick: () => void;
}

const NotificationItem: FC<NotificationItemProps> = ({ notification, onClick }) => {
    const menuItemSx = {
        fontWeight: notification.isRead ? "normal" : "bold",
        whiteSpace: "normal",
        display: "block",
        py: 1,
    };
    return (
        <MenuItem onClick={onClick} disabled={notification.isRead} sx={menuItemSx}>
            {notification._type === "simple" && (
                <SimpleNotificationComponent notification={notification as SimpleNotification} />
            )}
            {notification._type === "invite" && (
                <InviteNotificationComponent notification={notification as InviteNotification} />
            )}
            {notification._type === "checked" && (
                <CheckedNotificationComponent notification={notification as CheckedNotification} />
            )}
        </MenuItem>
    );
};
