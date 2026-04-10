import React, { FC, useState } from "react";
import { Badge, Box, IconButton } from "@mui/material";
import { NotificationMenu } from "./NotificationMenu";
import NotificationsIcon from "@mui/icons-material/Notifications";
import { useAuthContext } from "@/features/auth/context";
import { InvitationDialog } from "./InvitationDialog";
import { BaseNotification, InviteNotification } from "@/features/notifications/types";
import { toast } from "react-toastify";
import { useMarkNotificationsAsReadMutation, useNotificationsCountRequest } from "@/features/notifications/api";
import { useBundleInvitationReplyMutation } from "@/features/bundles/api";

type BundleInviteInfo = {
    bundleName: string;
    bundleShareCode: string;
    inviterName: string;
    notificationUuid: string;
};

export const NotificationButton: FC = () => {
    const [anchorEl, setAnchorEl] = useState<HTMLElement | undefined>(undefined);
    const { isAuthorized } = useAuthContext();
    const { data: statistics } = useNotificationsCountRequest();
    const handleClose = () => {
        setAnchorEl(undefined);
    };
    const handleOpen = (event: React.MouseEvent<HTMLElement>) => {
        setAnchorEl(event.currentTarget);
    };

    const markAsReadMutation = useMarkNotificationsAsReadMutation();
    const invitationReplyMutation = useBundleInvitationReplyMutation();
    const [bundleInviteInfo, setBundleInviteInfo] = useState<BundleInviteInfo>();

    const onInvitationDialogClose = (response: boolean | undefined) => {
        if (response != undefined && bundleInviteInfo != undefined) {
            const { bundleName, bundleShareCode, notificationUuid } = bundleInviteInfo;
            invitationReplyMutation.mutate(
                { shareCode: bundleShareCode, isAccepted: response },
                {
                    onSuccess: () => {
                        markAsReadMutation.mutate([notificationUuid]);
                        toast.success(`You have joined ${bundleName} bundle!`);
                    },
                    onError: () => {
                        toast.error(`You could not join ${bundleName} bundle due to some error...`);
                    },
                },
            );
        }
        setBundleInviteInfo(undefined);
    };

    // todo: rework
    const onNotificationClick = (notification: BaseNotification) => {
        const { _type, uuid } = notification;
        if (_type === "simple" || _type === "checked") {
            markAsReadMutation.mutate([uuid]);
        } else if (_type === "invite") {
            const { bundleName, bundleShareCode, inviterName } = notification as InviteNotification;
            setBundleInviteInfo({ bundleName, inviterName, bundleShareCode, notificationUuid: uuid });
        }
    };

    return (
        <Box>
            <InvitationDialog bundleInfo={bundleInviteInfo} onClose={onInvitationDialogClose} />
            <NotificationMenu
                notificationStatistics={statistics}
                onNotificationClick={onNotificationClick}
                anchorEl={anchorEl}
                onClose={handleClose}
            />
            {isAuthorized && (
                <IconButton
                    aria-label="show notifications"
                    aria-haspopup="true"
                    aria-expanded={Boolean(anchorEl)}
                    color={"primary"}
                    edge="end"
                    onClick={handleOpen}
                >
                    <Badge badgeContent={statistics?.unread || 0} color="primary">
                        <NotificationsIcon />
                    </Badge>
                </IconButton>
            )}
        </Box>
    );
};
