import type { BaseNotificationDto, NotificationStatistics } from "@/generated/notifier";

export type {
    BaseNotificationDto as BaseNotification,
    CheckedNotificationDto as CheckedNotification,
    InviteNotificationDto as InviteNotification,
    SimpleNotificationDto as SimpleNotification,
    NotificationStatistics,
} from "@/generated/notifier";

export type NotificationPage = {
    notifications: BaseNotificationDto[];
    statistics: NotificationStatistics;
};
