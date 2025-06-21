import { FC } from "react";
import { Avatar, Badge, Box, MenuItem, Stack, Typography } from "@mui/material";
import { SimpleNotification } from "../../types/notifications/SimpleNotification";

interface SimpleNotificationComponentProps {
    notification: SimpleNotification;
    onNotificationClick: (uuid: string) => void;
}

export const SimpleNotificationComponent: FC<SimpleNotificationComponentProps> = (
    { notification, onNotificationClick }
) => {
    const formatNotificationDate = (date: Date) => {
        const d = new Date(date);
        return d.toLocaleDateString('en-US', {
            month: 'short', day: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit'
        });
    };
    const onClick = () => onNotificationClick(notification.uuid);
    return (
        <MenuItem
            key={notification.uuid}
            sx={{ fontWeight: notification.isRead ? 'normal' : 'bold', whiteSpace: 'normal', display: 'block', py: 1 }}
            onClick={onClick}
        >
            <Stack direction={"row"} alignItems={"center"}>
                <Badge color="secondary" variant="dot" invisible={notification.isRead} sx={{mr: 2}}>
                    <Avatar alt="Remy Sharp" src="logo.png" />
                </Badge>
                <Box>
                    <Typography component={"span"} variant={"h6"} textAlign={"center"}>
                        {notification.title}
                    </Typography>
                    <Box>
                        <Box>
                            <Typography component={"span"} variant={"body1"}>
                                { notification.message }
                            </Typography>
                        </Box>
                        <Box>
                            <Typography component="span" variant="caption">
                                { formatNotificationDate(notification.createdAt) }
                                {" from "}
                                <u>
                                    { notification.source }
                                </u>
                            </Typography>
                        </Box>
                    </Box>
                </Box>
            </Stack>
        </MenuItem>
    );
};
