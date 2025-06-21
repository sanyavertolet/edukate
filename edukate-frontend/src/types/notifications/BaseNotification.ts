export interface BaseNotification {
    "_type": "base" | "simple";
    uuid: string;
    userId: string;
    isRead: boolean;
    createdAt: Date;
}
