import { useMutation, useQuery } from "@tanstack/react-query";
import { useAuthContext } from "@/features/auth/context";
import { defaultErrorHandler } from "@/lib/error-handler";
import { client } from "@/lib/axios";
import { queryKeys } from "@/lib/query-keys";
import { NotificationStatistics, BaseNotification } from "./types";

export function useNotificationsCountRequest(isRead?: boolean) {
    const { isAuthorized } = useAuthContext();
    return useQuery({
        queryKey: queryKeys.notifications.stats,
        enabled: isAuthorized,
        queryFn: async () => {
            try {
                const response = await client.get<NotificationStatistics>("/api/v1/notifications/count", {
                    params: { isRead },
                });
                return response.data;
            } catch (error) {
                throw defaultErrorHandler(error);
            }
        },
    });
}

export function useGetNotificationsRequest(isRead?: boolean, size: number = 10, page: number = 0) {
    const { isAuthorized } = useAuthContext();
    return useQuery({
        queryKey: queryKeys.notifications.all,
        enabled: isAuthorized,
        queryFn: async () => {
            try {
                const response = await client.get<BaseNotification[]>("/api/v1/notifications", {
                    params: { isRead, size, page },
                });
                return response.data;
            } catch (error) {
                throw defaultErrorHandler(error);
            }
        },
    });
}

export function useMarkNotificationsAsReadMutation() {
    return useMutation({
        mutationKey: ["notifications", "mark-as-read"],
        mutationFn: async (uuids: string[]) => {
            try {
                const response = await client.post("/api/v1/notifications/mark-as-read", uuids);
                return response.data as number;
            } catch (error) {
                throw defaultErrorHandler(error);
            }
        },
    });
}

export function useMarkAllNotificationsAsReadMutation() {
    return useMutation({
        mutationKey: ["notifications", "mark-all-as-read"],
        mutationFn: async () => {
            try {
                const response = await client.post("/api/v1/notifications/mark-all-as-read");
                return response.data as number;
            } catch (error) {
                throw defaultErrorHandler(error);
            }
        },
    });
}
