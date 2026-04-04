import { UtcIsoString } from "@/shared/utils/date-types";
import { CheckStatus } from "@/features/checks/types";

export type NotificationType = "base" | "simple" | "invite" | "checked";

export type BaseNotification = {
    _type: NotificationType;
    uuid: string;
    userId: string;
    isRead: boolean;
    createdAt: UtcIsoString;
};

export type SimpleNotification = BaseNotification & {
    title: string;
    message: string;
    source: string;
};

export type InviteNotification = BaseNotification & {
    inviterName: string;
    bundleName: string;
    bundleShareCode: string;
};

export type CheckedNotification = BaseNotification & {
    submissionId: string;
    problemId: string;
    status: CheckStatus;
};

export type NotificationStatistics = {
    unread: number;
    total: number;
};
