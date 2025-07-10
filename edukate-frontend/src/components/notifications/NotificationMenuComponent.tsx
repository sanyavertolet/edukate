import { ChangeEvent, FC, useMemo, useState } from "react";
import { useGetNotificationsRequest, useMarkAllNotificationsAsReadMutation } from "../../http/requests";
import { Box, Button, Divider, Menu, MenuItem, Pagination, Typography } from "@mui/material";
import { MarkEmailRead } from "@mui/icons-material";
import { NotificationStatistics } from "../../types/notification/NotificationStatistics";
import { BaseNotification } from "../../types/notification/BaseNotification";
import { SimpleNotificationComponent } from "./SimpleNotificationComponent";
import { SimpleNotification } from "../../types/notification/SimpleNotification";
import { InviteNotificationComponent } from "./InviteNotificationComponent";
import { InviteNotification } from "../../types/notification/InviteNotification";

interface NotificationListComponentProps {
    anchorEl?: HTMLElement;
    onClose: () => void;
    notificationStatistics: NotificationStatistics | null | undefined;
    onNotificationClick: (notification: BaseNotification) => void;
}

const defaultNotificationPageSize: number = 10;

function calculateNumberOfPage(statistics: NotificationStatistics | undefined | null) {
    return statistics ? Math.ceil(statistics.total / defaultNotificationPageSize) : 1;
}

export const NotificationMenuComponent: FC<NotificationListComponentProps> = (
    {anchorEl, onClose, notificationStatistics, onNotificationClick}
) => {
    const [page, setPage] = useState(1);
    const { data: notifications, refetch: refetchNotifications } = useGetNotificationsRequest(
        undefined,
        defaultNotificationPageSize,
        page - 1
    );

    const markAllAsReadMutation = useMarkAllNotificationsAsReadMutation();
    const handleMarkAllAsRead = () => { markAllAsReadMutation.mutate(); refetchNotifications().then(); };

    const onPaginationChange = (_: ChangeEvent<unknown>, newPage: number) => { setPage(newPage); };
    const isMenuOpen = Boolean(anchorEl);
    const numberOfPages = useMemo(() => calculateNumberOfPage(notificationStatistics), [notificationStatistics]);
    const notificationsMenuPrefixSx = { p: 1, display: 'flex', justifyContent: 'space-between', alignItems: 'center' };
    return (
        <Menu
            id="notifications-menu" open={isMenuOpen} onClose={onClose} anchorEl={anchorEl}
            anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
            transformOrigin={{ vertical: 'top', horizontal: 'right' }}
            MenuListProps={{ 'aria-labelledby': 'notifications-button' }}
            slotProps={{ paper: { style: { maxHeight: '400px', width: '350px' }} }}
        >
            <Box key="notifications-menu-header" sx={notificationsMenuPrefixSx}>
                <Typography variant="subtitle2" fontWeight="bold">
                    Notifications
                </Typography>
                <Button size="small" startIcon={<MarkEmailRead />} onClick={handleMarkAllAsRead}>
                    Mark all as read
                </Button>
            </Box>
            <Divider/>
            { notifications && notifications.map((notification) =>
                <NotificationComponent
                    key={notification.uuid}
                    notification={notification}
                    onClick={() => onNotificationClick(notification)}
                />
            )}
            { notifications && numberOfPages > 1 &&
                <Box key="notifications-menu-footer">
                    <Divider sx={{ my: 1 }}/>
                    <Pagination
                        count={numberOfPages} page={page} showFirstButton showLastButton onChange={onPaginationChange}
                        size={"small"} color={"primary"} sx={{ justifySelf: "center" }}
                    />
                </Box>
            }
        </Menu>
    )
};

interface NotificationComponentProps {
    notification: BaseNotification;
    onClick: () => void;
}

const NotificationComponent: FC<NotificationComponentProps> = ({ notification, onClick }) => {
    const menuItemSx = {
        fontWeight: notification.isRead ? 'normal' : 'bold', whiteSpace: 'normal', display: 'block', py: 1
    };
    return (
        <MenuItem onClick={onClick} disabled={notification.isRead} sx={menuItemSx}>
            { notification._type === "simple" && (
                <SimpleNotificationComponent notification={ notification as SimpleNotification }/>
            )}
            { notification._type === "invite" && (
                <InviteNotificationComponent notification={ notification as InviteNotification }/>
            )}
        </MenuItem>
    )
};
