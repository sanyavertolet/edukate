import { FC } from "react";
import { Avatar, Badge, Box, MenuItem, Stack, Typography } from "@mui/material";
import { InviteNotification } from "../../types/notification/InviteNotification";
import { formatDate } from "../../utils/utils";

interface InviteNotificationComponentProps {
    notification: InviteNotification;
    onClick: () => void;
    disabled?: boolean;
}

export const InviteNotificationComponent: FC<InviteNotificationComponentProps> = (
    { notification, onClick, disabled = false }
) => {
    return (
        <Box>
            <MenuItem
                key={notification.uuid} onClick={onClick} disabled={disabled}
                sx={{ fontWeight: notification.isRead ? 'normal' : 'bold', display: 'block', py: 1 }}
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
                                    Join a bundle { notification.bundleName }!
                                </Typography>
                            </Box>
                            <Box>
                                <Typography component="span" variant="caption">
                                    { formatDate(notification.createdAt) }
                                </Typography>
                            </Box>
                        </Box>
                    </Box>
                </Stack>
            </MenuItem>
        </Box>
    );
};
