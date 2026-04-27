import React, { FC, useState } from "react";
import { Badge, IconButton } from "@mui/material";
import NotificationsIcon from "@mui/icons-material/Notifications";
import { useNavigate } from "react-router-dom";
import { useAuthContext } from "@/features/auth/context";
import { InvitationDialog } from "./InvitationDialog";
import { NotificationPanel } from "./NotificationPanel";
import { BaseNotification, CheckedNotification, InviteNotification } from "@/features/notifications/types";
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
    const navigate = useNavigate();

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

    const onNotificationClick = (notification: BaseNotification) => {
        const { _type, uuid } = notification;
        if (_type === "simple") {
            markAsReadMutation.mutate([uuid]);
        } else if (_type === "checked") {
            markAsReadMutation.mutate([uuid]);
            handleClose();
            void navigate(`/submissions/${String((notification as CheckedNotification).submissionId)}`);
        } else if (_type === "invite") {
            const { problemSetName, problemSetShareCode, inviterName } = notification as InviteNotification;
            setProblemSetInviteInfo({ problemSetName, inviterName, problemSetShareCode, notificationUuid: uuid });
        }
    };

    if (!isAuthorized) return null;

    return (
        <>
            <InvitationDialog problemSetInfo={problemSetInviteInfo} onClose={onInvitationDialogClose} />
            <NotificationPanel
                onNotificationClick={onNotificationClick}
                onMarkAsRead={(n) => {
                    markAsReadMutation.mutate([n.uuid]);
                }}
                anchorEl={anchorEl}
                onClose={handleClose}
            />
            <IconButton
                aria-label="show notifications"
                aria-haspopup="true"
                aria-expanded={Boolean(anchorEl)}
                color="primary"
                edge="end"
                onClick={handleOpen}
            >
                <Badge badgeContent={page?.statistics.unread || 0} color="primary">
                    <NotificationsIcon />
                </Badge>
            </IconButton>
        </>
    );
};
