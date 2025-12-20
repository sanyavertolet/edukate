import { useMutation, useQuery } from "@tanstack/react-query";
import { useAuthContext } from "../../components/auth/AuthContextProvider";
import { defaultErrorHandler } from "../utils";
import { client } from "../client";
import { NotificationStatistics } from "../../types/notification/NotificationStatistics";
import { BaseNotification } from "../../types/notification/BaseNotification";

export function useNotificationsCountRequest(isRead?: boolean) {
    const { isAuthorized } = useAuthContext();
    return useQuery({
        queryKey: ['notifications', 'count', isRead, isAuthorized],
        queryFn: async () => {
            if (!isAuthorized) {
                return null;
            }
            try {
                const response = await client.get<NotificationStatistics>('/api/v1/notifications/count', {
                    params: { isRead }
                });
                return response.data;
            } catch (error) {
                throw defaultErrorHandler(error);
            }
        }
    })
}

export function useGetNotificationsRequest(isRead?: boolean, size: number = 10, page: number = 0) {
    const { isAuthorized } = useAuthContext();
    return useQuery({
        queryKey: ['notifications', 'get', isRead, size, page, isAuthorized],
        queryFn: async () => {
            if (!isAuthorized) {
                return null;
            }
            try {
                const response = await client.get<BaseNotification[]>('/api/v1/notifications', {
                    params: {isRead, size, page}
                });
                return response.data;
            } catch (error) {
                throw defaultErrorHandler(error);
            }
        }
    })
}

export function useMarkNotificationsAsReadMutation() {
    const { isAuthorized } = useAuthContext();
    return useMutation({
        mutationKey: ['notifications', 'mark-as-read'],
        mutationFn: async (uuids: string[]) => {
            if (!isAuthorized) {
                return null;
            }
            try {
                const response = await client.post('/api/v1/notifications/mark-as-read', uuids);
                return response.data as number;
            } catch (error) {
                throw defaultErrorHandler(error);
            }
        }
    })
}

export function useMarkAllNotificationsAsReadMutation() {
    const { isAuthorized } = useAuthContext();
    return useMutation({
        mutationKey: ['notifications', 'mark-all-as-read'],
        mutationFn: async () => {
            if (!isAuthorized) {
                return null;
            }
            try {
                const response = await client.post('/api/v1/notifications/mark-all-as-read');
                return response.data as number;
            } catch (error) {
                throw defaultErrorHandler(error);
            }
        }
    })
}