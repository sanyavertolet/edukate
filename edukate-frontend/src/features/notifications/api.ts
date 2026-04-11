import { useMutation, useQuery } from "@tanstack/react-query";
import { getNotifications, markAsRead, markAsRead1 } from "@/generated/notifier";
import { useAuthContext } from "@/features/auth/context";
import { queryClient } from "@/lib/query-client";
import { queryKeys } from "@/lib/query-keys";
import type { NotificationPage } from "@/features/notifications/types";

const NOTIFICATIONS_STALE_TIME = 30_000;

export function useGetNotificationsRequest(isRead?: boolean, size: number = 10, page: number = 0) {
    const { isAuthorized } = useAuthContext();
    return useQuery({
        queryKey: queryKeys.notifications.list(isRead, page, size),
        enabled: isAuthorized,
        staleTime: NOTIFICATIONS_STALE_TIME,
        queryFn: ({ signal }) =>
            getNotifications({ isRead, size: String(size), page: String(page) }, signal) as Promise<NotificationPage>,
    });
}

export function useMarkNotificationsAsReadMutation() {
    return useMutation({
        mutationFn: (uuids: string[]) => markAsRead(uuids),
        onSuccess: () => queryClient.invalidateQueries({ queryKey: queryKeys.notifications.all }).finally(),
    });
}

export function useMarkAllNotificationsAsReadMutation() {
    return useMutation({
        mutationFn: () => markAsRead1(),
        onSuccess: () => queryClient.invalidateQueries({ queryKey: queryKeys.notifications.all }).finally(),
    });
}
