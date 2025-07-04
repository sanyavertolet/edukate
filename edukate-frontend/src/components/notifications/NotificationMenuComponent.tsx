import { ChangeEvent, FC, useMemo, useState } from "react";
import {
    useGetNotificationsRequest,
    useMarkAllNotificationsAsReadMutation,
    useMarkNotificationsAsReadMutation
} from "../../http/requests";
import { Box, Button, Divider, Menu, Pagination, Typography } from "@mui/material";
import { NotificationComponent } from "./NotificationComponent";
import { MarkEmailRead } from "@mui/icons-material";
import { NotificationStatistics } from "../../types/notification/NotificationStatistics";

interface NotificationListComponentProps {
    anchorEl?: HTMLElement;
    onClose: () => void;
    notificationStatistics: NotificationStatistics | null | undefined;
}

const defaultNotificationPageSize: number = 10;

function calculateNumberOfPage(statistics: NotificationStatistics | undefined | null) {
    return statistics ? Math.ceil(statistics.total / defaultNotificationPageSize) : 1;
}

export const NotificationMenuComponent: FC<NotificationListComponentProps> = (
    {anchorEl, onClose, notificationStatistics}
) => {
    const [page, setPage] = useState(1);
    const { data: notifications, refetch: refetchNotifications } = useGetNotificationsRequest(
        undefined,
        defaultNotificationPageSize,
        page - 1
    );

    const markAllAsReadMutation = useMarkAllNotificationsAsReadMutation();
    const handleMarkAllAsRead = () => {
        markAllAsReadMutation.mutate();
        refetchNotifications().then();
    };

    const markAsReadMutation = useMarkNotificationsAsReadMutation();
    const onNotificationClick = (uuid: string) => {
        markAsReadMutation.mutate([uuid]);
        refetchNotifications().then();
    };
    const onPaginationChange = (_: ChangeEvent<unknown>, newPage: number) => { setPage(newPage); };
    const isMenuOpen = Boolean(anchorEl);
    const numberOfPages = useMemo(() => calculateNumberOfPage(notificationStatistics), [notificationStatistics]);
    return (
        <Menu
            id="notifications-menu"
            anchorEl={anchorEl}
            anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
            transformOrigin={{ vertical: 'top', horizontal: 'right' }}
            open={isMenuOpen}
            onClose={onClose}
            MenuListProps={{ 'aria-labelledby': 'notifications-button' }}
            slotProps={{ paper: { style: { maxHeight: '400px', width: '350px' }} }}
        >
            <Box sx={{ p: 1, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <Typography variant="subtitle2" fontWeight="bold">
                    Notifications
                </Typography>
                <Button size="small" startIcon={<MarkEmailRead />} onClick={handleMarkAllAsRead}>
                    Mark all as read
                </Button>
            </Box>
            <Divider />
            { notifications && notifications.map((notification) =>
                <NotificationComponent notification={notification} onNotificationClick={onNotificationClick}/>
            )}
            { notifications && numberOfPages > 1 &&
                <Box>
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
