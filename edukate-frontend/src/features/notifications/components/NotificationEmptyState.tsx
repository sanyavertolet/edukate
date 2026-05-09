import { FC } from "react";
import { Box, Typography } from "@mui/material";
import { NotificationsNoneOutlined } from "@mui/icons-material";
import { useTranslation } from "react-i18next";

interface NotificationEmptyStateProps {
    filter: "all" | "unread";
}

export const NotificationEmptyState: FC<NotificationEmptyStateProps> = ({ filter }) => {
    const { t } = useTranslation();
    return (
        <Box sx={{ display: "flex", flexDirection: "column", alignItems: "center", py: 4, px: 2 }}>
            <NotificationsNoneOutlined sx={{ fontSize: 48, color: "text.secondary", mb: 1 }} />
            <Typography variant="body1" color="text.primary">
                {filter === "all" ? t("no_notifications") : t("no_unread_notifications")}
            </Typography>
            <Typography variant="body2" color="text.secondary">
                {t("all_caught_up")}
            </Typography>
        </Box>
    );
};
