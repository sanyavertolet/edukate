import { FC } from "react";
import { Popover } from "@mui/material";
import { frostedGlass } from "@/shared/components/Styled";
import { BaseNotification } from "@/features/notifications/types";
import { NotificationContent } from "./NotificationContent";

interface NotificationPanelProps {
    anchorEl?: HTMLElement;
    onClose: () => void;
    onNotificationClick: (notification: BaseNotification) => void;
    onMarkAsRead?: (notification: BaseNotification) => void;
}

export const NotificationPanel: FC<NotificationPanelProps> = ({ anchorEl, onClose, onNotificationClick, onMarkAsRead }) => (
    <Popover
        open={Boolean(anchorEl)}
        anchorEl={anchorEl}
        onClose={onClose}
        anchorOrigin={{ vertical: "bottom", horizontal: "right" }}
        transformOrigin={{ vertical: "top", horizontal: "right" }}
        slotProps={{
            paper: {
                sx: (theme) => ({
                    width: 400,
                    maxWidth: 400,
                    maxHeight: 520,
                    display: "flex",
                    flexDirection: "column",
                    ...frostedGlass(theme),
                    border: 1,
                    borderColor: "divider",
                }),
            },
        }}
    >
        <NotificationContent onNotificationClick={onNotificationClick} onMarkAsRead={onMarkAsRead} />
    </Popover>
);
