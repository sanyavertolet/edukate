import { FC } from "react";
import { useGetNotificationsRequest, useMarkNotificationsAsReadMutation } from "../../http/requests";
import { Box, Button, Divider, Menu, Typography } from "@mui/material";
import { NotificationComponent } from "./NotificationComponent";
import { MarkEmailRead } from "@mui/icons-material";

interface NotificationListComponentProps {
    anchorEl?: HTMLElement;
    onClose: () => void;
}

export const NotificationMenuComponent: FC<NotificationListComponentProps> = ({anchorEl, onClose}) => {
    const isMenuOpen = Boolean(anchorEl);

    const markAsReadMutation = useMarkNotificationsAsReadMutation();
    const handleMarkAllAsRead = () => { console.log("wip;stub") };

    const { data: notifications, refetch: refetchNotifications } = useGetNotificationsRequest();
    const onNotificationClick = (uuid: string) => {
        markAsReadMutation.mutate([uuid]);
        refetchNotifications().then();
    };

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
        </Menu>
    )
};
