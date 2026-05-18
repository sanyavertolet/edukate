import { FC, useState, MouseEvent } from "react";
import { Badge, IconButton } from "@mui/material";
import NotificationsIcon from "@mui/icons-material/Notifications";
import { useAuthContext } from "@/features/auth/context";
import { InvitationDialog } from "./InvitationDialog";
import { NotificationPanel } from "./NotificationPanel";
import { NotificationDrawer } from "./NotificationDrawer";
import { BaseNotification, CheckedNotification, InviteNotification } from "@/features/notifications/types";
import { toast } from "react-toastify";
import { useGetNotificationsRequest, useMarkNotificationsAsReadMutation } from "@/features/notifications/api";
import { useProblemSetInvitationReplyMutation } from "@/features/problem-sets/api";
import { useSubmissionQuery } from "@/features/submissions/api";
import { SubmissionDrawer } from "@/features/submissions/components/SubmissionDrawer";
import { useTranslation } from "react-i18next";
import { useDeviceContext } from "@/shared/context/DeviceContext";

type ProblemSetInviteInfo = {
    problemSetName: string;
    problemSetShareCode: string;
    inviterName: string;
    notificationUuid: string;
};

export const NotificationButton: FC = () => {
    const { t } = useTranslation();
    const { isMobile } = useDeviceContext();
    const [anchorEl, setAnchorEl] = useState<HTMLElement | undefined>(undefined);
    const { isAuthorized } = useAuthContext();
    const { data: page } = useGetNotificationsRequest();

    const handleClose = () => {
        setAnchorEl(undefined);
    };
    const handleOpen = (event: MouseEvent<HTMLElement>) => {
        setAnchorEl(event.currentTarget);
    };

    const markAsReadMutation = useMarkNotificationsAsReadMutation();
    const invitationReplyMutation = useProblemSetInvitationReplyMutation();
    const [problemSetInviteInfo, setProblemSetInviteInfo] = useState<ProblemSetInviteInfo>();
    const [drawerSubmissionId, setDrawerSubmissionId] = useState<string | undefined>(undefined);
    const { data: drawerSubmission } = useSubmissionQuery(drawerSubmissionId);

    const onInvitationDialogClose = (response: boolean | undefined) => {
        if (response != undefined && problemSetInviteInfo != undefined) {
            const { problemSetName, problemSetShareCode, notificationUuid } = problemSetInviteInfo;
            invitationReplyMutation.mutate(
                { shareCode: problemSetShareCode, isAccepted: response },
                {
                    onSuccess: () => {
                        markAsReadMutation.mutate([notificationUuid]);
                        toast.success(t("joined_problem_set_success", { name: problemSetName }));
                    },
                    onError: () => {
                        toast.error(t("joined_problem_set_error", { name: problemSetName }));
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
            setDrawerSubmissionId(String((notification as CheckedNotification).submissionId));
        } else if (_type === "invite") {
            const { problemSetName, problemSetShareCode, inviterName } = notification as InviteNotification;
            setProblemSetInviteInfo({ problemSetName, inviterName, problemSetShareCode, notificationUuid: uuid });
        }
    };

    if (!isAuthorized) return null;

    return (
        <>
            <InvitationDialog problemSetInfo={problemSetInviteInfo} onClose={onInvitationDialogClose} />
            <SubmissionDrawer
                submission={drawerSubmission ?? null}
                onClose={() => {
                    setDrawerSubmissionId(undefined);
                }}
            />
            {isMobile ? (
                <NotificationDrawer
                    open={Boolean(anchorEl)}
                    onClose={handleClose}
                    onNotificationClick={onNotificationClick}
                    onMarkAsRead={(n) => {
                        markAsReadMutation.mutate([n.uuid]);
                    }}
                />
            ) : (
                <NotificationPanel
                    anchorEl={anchorEl}
                    onClose={handleClose}
                    onNotificationClick={onNotificationClick}
                    onMarkAsRead={(n) => {
                        markAsReadMutation.mutate([n.uuid]);
                    }}
                />
            )}
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
