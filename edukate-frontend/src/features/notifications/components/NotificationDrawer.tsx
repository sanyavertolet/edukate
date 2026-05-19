import { FC } from "react";
import { Box } from "@mui/material";
import { SwipeableDrawer } from "@/shared/components/Styled";
import { BaseNotification } from "@/features/notifications/types";
import { NotificationContent } from "./NotificationContent";

interface NotificationDrawerProps {
    open: boolean;
    onClose: () => void;
    onNotificationClick: (notification: BaseNotification) => void;
    onMarkAsRead?: (notification: BaseNotification) => void;
}

export const NotificationDrawer: FC<NotificationDrawerProps> = ({ open, onClose, onNotificationClick, onMarkAsRead }) => (
    <SwipeableDrawer
        anchor="bottom"
        open={open}
        onClose={onClose}
        onOpen={() => {}}
        disableSwipeToOpen
        slotProps={{
            paper: {
                sx: {
                    borderRadius: "16px 16px 0 0",
                    height: "90vh",
                    display: "flex",
                    flexDirection: "column",
                    overflow: "hidden",
                    pb: "env(safe-area-inset-bottom)",
                },
            },
        }}
    >
        <Box sx={{ display: "flex", justifyContent: "center", py: 1, bgcolor: "background.paper" }}>
            <Box sx={{ width: 32, height: 4, borderRadius: 2, bgcolor: "divider" }} />
        </Box>
        <Box sx={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column", overflow: "hidden" }}>
            <NotificationContent onNotificationClick={onNotificationClick} onMarkAsRead={onMarkAsRead} />
        </Box>
    </SwipeableDrawer>
);
