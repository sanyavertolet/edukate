import { UtcIsoString } from "../common/DateTypes";

export type NotificationType = "base" | "simple" | "invite" | "checked";

export type BaseNotification = {
    "_type": NotificationType,
    uuid: string,
    userId: string,
    isRead: boolean,
    createdAt: UtcIsoString,
};
