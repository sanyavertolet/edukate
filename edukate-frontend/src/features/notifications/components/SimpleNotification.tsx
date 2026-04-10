import { FC } from "react";
import { Avatar, Badge, Box, Stack, Typography } from "@mui/material";
import { SimpleNotification } from "@/features/notifications/types";
import { formatDate } from "@/shared/utils/date";

interface SimpleNotificationProps {
    notification: SimpleNotification;
}

export const SimpleNotificationComponent: FC<SimpleNotificationProps> = ({ notification }) => {
    return (
        <Stack direction={"row"} alignItems={"center"}>
            <Badge color="secondary" variant="dot" invisible={notification.isRead} sx={{ mr: 2 }}>
                <Avatar alt="edukate" src="logo.png" />
            </Badge>
            <Box>
                <Typography component={"span"} variant={"h6"} textAlign={"center"}>
                    {notification.title}
                </Typography>
                <Box>
                    <Box>
                        <Typography component={"span"} variant={"body1"}>
                            {notification.message}
                        </Typography>
                    </Box>
                    <Box>
                        <Typography component="span" variant="caption">
                            {formatDate(notification.createdAt)}
                            {" from "}
                            <u>{notification.source}</u>
                        </Typography>
                    </Box>
                </Box>
            </Box>
        </Stack>
    );
};
