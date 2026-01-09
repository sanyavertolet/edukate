import React, { FC, useState } from "react";
import { Badge, Box, IconButton } from "@mui/material";
import { NotificationMenuComponent } from "./NotificationMenuComponent";
import NotificationsIcon from "@mui/icons-material/Notifications";
import { useAuthContext } from "../auth/AuthContextProvider";
import { InvitationDialog } from "./InvitationDialog";
import { BaseNotification } from "../../types/notification/BaseNotification";
import { InviteNotification } from "../../types/notification/InviteNotification";
import { toast } from "react-toastify";
import { useMarkNotificationsAsReadMutation, useNotificationsCountRequest } from "../../http/requests/notifications";
import { useBundleInvitationReplyMutation } from "../../http/requests/bundles";

type BundleInviteInfo = {
    bundleName: string;
    bundleShareCode: string;
    inviterName: string;
    notificationUuid: string;
};

export const NotificationButton: FC = () => {
    const [anchorEl, setAnchorEl] = useState<HTMLElement | undefined>(undefined);
    const { isAuthorized } = useAuthContext();
    const { data: statistics } = useNotificationsCountRequest(false);
    const handleClose = () => setAnchorEl(undefined);
    const handleOpen = (event: React.MouseEvent<HTMLElement>) => { setAnchorEl(event.currentTarget); };

    const markAsReadMutation = useMarkNotificationsAsReadMutation();
    const invitationReplyMutation = useBundleInvitationReplyMutation();
    const [bundleInviteInfo, setBundleInviteInfo] = useState<BundleInviteInfo>();

    const onInvitationDialogClose = (response: boolean | undefined) => {
        if (response != undefined && bundleInviteInfo != undefined) {
            const { bundleName, bundleShareCode, notificationUuid } = bundleInviteInfo;
            invitationReplyMutation.mutate({ shareCode: bundleShareCode, isAccepted: response }, {
                onSuccess: () => {
                    markAsReadMutation.mutate([notificationUuid]);
                    toast.success(`You have joined ${bundleName} bundle!`);
                },
                onError: () => {
                    toast.error(`You could not join ${bundleName} bundle due to some error...`);
                }
            });
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
            setBundleInviteInfo({bundleName, inviterName, bundleShareCode, notificationUuid: uuid});
        }
    };

    return (
        <Box>
            <InvitationDialog bundleInfo={ bundleInviteInfo } onClose={onInvitationDialogClose}/>
            <NotificationMenuComponent
                notificationStatistics={statistics} onNotificationClick={onNotificationClick}
                anchorEl={anchorEl} onClose={handleClose}
            />
            { isAuthorized && (
                <IconButton aria-label="show notifications" aria-haspopup="true"
                            color={"primary"} edge="end" onClick={handleOpen}>
                    <Badge badgeContent={ statistics?.unread || 0 } color="primary">
                        <NotificationsIcon />
                    </Badge>
                </IconButton>
            )}
        </Box>
    );
};
