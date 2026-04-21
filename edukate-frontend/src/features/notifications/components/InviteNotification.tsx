import { FC } from "react";
import { Avatar, Badge, Box, Stack, Typography } from "@mui/material";
import { InviteNotification } from "@/features/notifications/types";
import { formatDate } from "@/shared/utils/date";

interface InviteNotificationProps {
    notification: InviteNotification;
}

export const InviteNotificationComponent: FC<InviteNotificationProps> = ({ notification }) => {
    return (
        <Stack direction={"row"} alignItems={"center"}>
            <Badge color="secondary" variant="dot" invisible={notification.isRead} sx={{ mr: 2 }}>
                <Avatar alt="edukate" />
            </Badge>
            <Box>
                <Typography component={"span"} variant={"h6"} textAlign={"center"} color={"primary"}>
                    {notification.inviterName} invites you!
                </Typography>
                <Box>
                    <Box>
                        <Typography component={"span"} variant={"body1"}>
                            Join a problem set {notification.problemSetName}!
                        </Typography>
                    </Box>
                    <Box>
                        <Typography component="span" variant="caption">
                            {formatDate(notification.createdAt)}
                        </Typography>
                    </Box>
                </Box>
            </Box>
        </Stack>
    );
};
