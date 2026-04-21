import React, { FC, useState } from "react";
import { Badge, Box, IconButton } from "@mui/material";
import { NotificationMenu } from "./NotificationMenu";
import NotificationsIcon from "@mui/icons-material/Notifications";
import { useAuthContext } from "@/features/auth/context";
import { InvitationDialog } from "./InvitationDialog";
import { BaseNotification, InviteNotification } from "@/features/notifications/types";
import { toast } from "react-toastify";
import { useGetNotificationsRequest, useMarkNotificationsAsReadMutation } from "@/features/notifications/api";
import { useProblemSetInvitationReplyMutation } from "@/features/problem-sets/api";

type ProblemSetInviteInfo = {
    problemSetName: string;
    problemSetShareCode: string;
    inviterName: string;
    notificationUuid: string;
};

export const NotificationButton: FC = () => {
    const [anchorEl, setAnchorEl] = useState<HTMLElement | undefined>(undefined);
    const { isAuthorized } = useAuthContext();
    const { data: page } = useGetNotificationsRequest();
    const handleClose = () => {
        setAnchorEl(undefined);
    };
    const handleOpen = (event: React.MouseEvent<HTMLElement>) => {
        setAnchorEl(event.currentTarget);
    };

    const markAsReadMutation = useMarkNotificationsAsReadMutation();
    const invitationReplyMutation = useProblemSetInvitationReplyMutation();
    const [problemSetInviteInfo, setProblemSetInviteInfo] = useState<ProblemSetInviteInfo>();

    const onInvitationDialogClose = (response: boolean | undefined) => {
        if (response != undefined && problemSetInviteInfo != undefined) {
            const { problemSetName, problemSetShareCode, notificationUuid } = problemSetInviteInfo;
            invitationReplyMutation.mutate(
                { shareCode: problemSetShareCode, isAccepted: response },
                {
                    onSuccess: () => {
                        markAsReadMutation.mutate([notificationUuid]);
                        toast.success(`You have joined ${problemSetName} problem set!`);
                    },
                    onError: () => {
                        toast.error(`You could not join ${problemSetName} problem set due to some error...`);
                    },
                },
            );
        }
        setProblemSetInviteInfo(undefined);
    };

    // todo: rework
    const onNotificationClick = (notification: BaseNotification) => {
        const { _type, uuid } = notification;
        if (_type === "simple" || _type === "checked") {
            markAsReadMutation.mutate([uuid]);
        } else if (_type === "invite") {
            const { problemSetName, problemSetShareCode, inviterName } = notification as InviteNotification;
            setProblemSetInviteInfo({ problemSetName, inviterName, problemSetShareCode, notificationUuid: uuid });
        }
    };

    return (
        <Box>
            <InvitationDialog problemSetInfo={problemSetInviteInfo} onClose={onInvitationDialogClose} />
            <NotificationMenu onNotificationClick={onNotificationClick} anchorEl={anchorEl} onClose={handleClose} />
            {isAuthorized && (
                <IconButton
                    aria-label="show notifications"
                    aria-haspopup="true"
                    aria-expanded={Boolean(anchorEl)}
                    color={"primary"}
                    edge="end"
                    onClick={handleOpen}
                >
                    <Badge badgeContent={page?.statistics.unread || 0} color="primary">
                        <NotificationsIcon />
                    </Badge>
                </IconButton>
            )}
        </Box>
    );
};
