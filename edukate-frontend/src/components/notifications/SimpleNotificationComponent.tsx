import { FC } from "react";
import { Avatar, Badge, Box, MenuItem, Stack, Typography } from "@mui/material";
import { SimpleNotification } from "../../types/notification/SimpleNotification";
import { formatDate } from "../../utils/utils";

interface SimpleNotificationComponentProps {
    notification: SimpleNotification;
    onNotificationClick: (uuid: string) => void;
}

export const SimpleNotificationComponent: FC<SimpleNotificationComponentProps> = (
    { notification, onNotificationClick }
) => {
    const onClick = () => onNotificationClick(notification.uuid);
    return (
        <MenuItem
            key={notification.uuid}
            sx={{ fontWeight: notification.isRead ? 'normal' : 'bold', whiteSpace: 'normal', display: 'block', py: 1 }}
            onClick={onClick}
        >
            <Stack direction={"row"} alignItems={"center"}>
                <Badge color="secondary" variant="dot" invisible={notification.isRead} sx={{mr: 2}}>
                    <Avatar alt="edukate" src="logo.png" />
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
                                { formatDate(notification.createdAt) }
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
