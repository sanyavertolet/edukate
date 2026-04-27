import { FC } from "react";
import { Box, Typography } from "@mui/material";
import { NotificationsNoneOutlined } from "@mui/icons-material";

interface NotificationEmptyStateProps {
    filter: "all" | "unread";
}

export const NotificationEmptyState: FC<NotificationEmptyStateProps> = ({ filter }) => (
    <Box sx={{ display: "flex", flexDirection: "column", alignItems: "center", py: 4, px: 2 }}>
        <NotificationsNoneOutlined sx={{ fontSize: 48, color: "text.secondary", mb: 1 }} />
        <Typography variant="body1" color="text.primary">
            {filter === "all" ? "No notifications" : "No unread notifications"}
        </Typography>
        <Typography variant="body2" color="text.secondary">
            You're all caught up!
        </Typography>
    </Box>
);
