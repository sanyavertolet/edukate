import { UtcIsoString } from "../common/DateTypes";

export interface BaseNotification {
    "_type": "base" | "simple" | "invite";
    uuid: string;
    userId: string;
    isRead: boolean;
    createdAt: UtcIsoString;
}
