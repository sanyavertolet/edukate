import { FC } from "react";
import { Avatar, Badge, Box, MenuItem, Stack, Typography } from "@mui/material";
import { InviteNotification } from "../../types/notification/InviteNotification";

interface InviteNotificationComponentProps {
    notification: InviteNotification;
    onNotificationClick: (uuid: string) => void;
}

export const InviteNotificationComponent: FC<InviteNotificationComponentProps> = (
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
                    <Avatar alt="edukate" />
                </Badge>
                <Box>
                    <Typography component={"span"} variant={"h6"} textAlign={"center"} color={"primary"}>
                        { notification.inviter } invites you!
                    </Typography>
                    <Box>
                        <Box>
                            <Typography component={"span"} variant={"body1"}>
                                Join a bundle { notification.bundleName } with share code
                                <code> { notification.bundleShareCode }!</code>
                            </Typography>
                        </Box>
                        <Box>
                            <Typography component="span" variant="caption">
                                { formatNotificationDate(notification.createdAt) }
                            </Typography>
                        </Box>
                    </Box>
                </Box>
            </Stack>
        </MenuItem>
    );
};
