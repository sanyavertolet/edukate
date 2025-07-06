import {FC, useState} from "react";
import { Avatar, Badge, Box, MenuItem, Stack, Typography } from "@mui/material";
import { InviteNotification } from "../../types/notification/InviteNotification";
import { InvitationDialog } from "./InvitationDialog";
import { useBundleInvitationReplyMutation } from "../../http/requests";
import { formatDate } from "../../utils/utils";

interface InviteNotificationComponentProps {
    notification: InviteNotification;
    markAsRead: (uuid: string) => void;
}

export const InviteNotificationComponent: FC<InviteNotificationComponentProps> = (
    { notification, markAsRead }
) => {
    const markNotificationAsRead = () => markAsRead(notification.uuid);
    const invitationReplyMutation = useBundleInvitationReplyMutation();
    const [isInvitationDialogOpen, setIsInvitationDialogOpen] = useState(false);
    const onInvitationDialogClose = (response: boolean | undefined) => {
        if (response != undefined) {
            invitationReplyMutation.mutate({shareCode: notification.bundleShareCode, isAccepted: response}, {
                onSuccess: () => { markNotificationAsRead() }
            });
        }
        setIsInvitationDialogOpen(false);
    }
    return (
        <Box>
            <InvitationDialog
                open={isInvitationDialogOpen}
                bundleName={notification.bundleName}
                inviterName={notification.inviter}
                onClose={onInvitationDialogClose}
            />
        <MenuItem
            key={notification.uuid}
            sx={{ fontWeight: notification.isRead ? 'normal' : 'bold', whiteSpace: 'normal', display: 'block', py: 1 }}
            onClick={() => setIsInvitationDialogOpen(true)}
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
