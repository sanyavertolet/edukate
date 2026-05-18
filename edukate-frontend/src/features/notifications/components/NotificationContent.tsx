import { FC, SyntheticEvent, useEffect, useRef, useState } from "react";
import {
    Badge,
    Box,
    Card,
    CardHeader,
    CircularProgress,
    Divider,
    IconButton,
    List,
    ListSubheader,
    Tab,
    Tabs,
    Tooltip,
} from "@mui/material";
import { MarkEmailRead } from "@mui/icons-material";
import { useGetNotificationsRequest, useMarkAllNotificationsAsReadMutation } from "@/features/notifications/api";
import { BaseNotification } from "@/features/notifications/types";
import { parseDate } from "@/shared/utils/date";
import { NotificationListItem } from "./NotificationListItem";
import { NotificationEmptyState } from "./NotificationEmptyState";
import { NotificationSkeleton } from "./NotificationSkeleton";
import { useTranslation } from "react-i18next";

const DEFAULT_PAGE_SIZE = 10;
const PAGE_INCREMENT = 10;

type FilterTab = "all" | "unread";

type DateGroup = { label: string; notifications: BaseNotification[] };

function groupByDate(notifications: BaseNotification[]): DateGroup[] {
    const now = new Date();
    const todayStart = new Date(now.getFullYear(), now.getMonth(), now.getDate()).getTime();
    const yesterdayStart = todayStart - 86_400_000;

    const groups: Record<string, BaseNotification[]> = {};
    const order: string[] = [];

    for (const n of notifications) {
        const ts = parseDate(n.createdAt).getTime();
        let label: string;
        if (ts >= todayStart) {
            label = "today";
        } else if (ts >= yesterdayStart) {
            label = "yesterday";
        } else {
            label = "earlier";
        }

        if (!(label in groups)) {
            groups[label] = [];
            order.push(label);
        }
        groups[label].push(n);
    }

    return order.map((label) => ({ label, notifications: groups[label] }));
}

interface NotificationContentProps {
    onNotificationClick: (notification: BaseNotification) => void;
    onMarkAsRead?: (notification: BaseNotification) => void;
}

export const NotificationContent: FC<NotificationContentProps> = ({ onNotificationClick, onMarkAsRead }) => {
    const { t } = useTranslation();
    const [filter, setFilter] = useState<FilterTab>("all");
    const [pageSize, setPageSize] = useState(DEFAULT_PAGE_SIZE);

    const isRead = filter === "unread" ? false : undefined;
    const { data: notificationPage, isLoading, isFetching } = useGetNotificationsRequest(isRead, pageSize, 0);
    const notifications = notificationPage?.notifications;
    const stats = notificationPage?.statistics;

    const markAllAsReadMutation = useMarkAllNotificationsAsReadMutation();

    const handleTabChange = (_: SyntheticEvent, newValue: FilterTab) => {
        setFilter(newValue);
        setPageSize(DEFAULT_PAGE_SIZE);
    };

    const totalForFilter = filter === "all" ? stats?.total : stats?.unread;
    const hasMore = notifications != null && totalForFilter != null && notifications.length < totalForFilter;
    const isLoadingMore = isFetching && !isLoading;

    const scrollContainerRef = useRef<HTMLDivElement>(null);
    const sentinelRef = useRef<HTMLDivElement>(null);

    // Disconnect observer while fetching to avoid triggering multiple increments;
    // reconnect when data settles so the sentinel is re-evaluated.
    useEffect(() => {
        if (!sentinelRef.current || !hasMore || isFetching) return;

        const observer = new IntersectionObserver(
            ([entry]) => {
                if (entry.isIntersecting) {
                    setPageSize((prev) => prev + PAGE_INCREMENT);
                }
            },
            { root: scrollContainerRef.current, threshold: 0.1 },
        );

        observer.observe(sentinelRef.current);
        return () => {
            observer.disconnect();
        };
    }, [hasMore, isFetching]);

    const dateGroups = notifications && notifications.length > 0 ? groupByDate(notifications) : [];

    return (
        <Card
            elevation={0}
            sx={{ display: "flex", flexDirection: "column", overflow: "hidden", height: "100%", bgcolor: "transparent" }}
        >
            <CardHeader
                title={t("notifications_title")}
                slotProps={{ title: { variant: "subtitle1", fontWeight: "bold" } }}
                action={
                    <Tooltip title={t("mark_all_read")}>
                        <IconButton
                            size="small"
                            disabled={markAllAsReadMutation.isPending}
                            onClick={() => {
                                markAllAsReadMutation.mutate();
                            }}
                            aria-label={t("mark_all_read")}
                        >
                            <MarkEmailRead fontSize="small" />
                        </IconButton>
                    </Tooltip>
                }
                sx={{ py: 1.5, px: 2 }}
            />
            <Divider />
            <Tabs
                value={filter}
                onChange={handleTabChange}
                variant="fullWidth"
                sx={{ minHeight: 36, "& .MuiTab-root": { minHeight: 36, py: 0.5 } }}
            >
                <Tab label={t("notifications_tab_all")} value="all" />
                <Tab
                    label={
                        <Badge badgeContent={stats?.unread} color="primary" max={99}>
                            {t("notifications_tab_unread")}
                        </Badge>
                    }
                    value="unread"
                />
            </Tabs>
            <Divider />
            <Box ref={scrollContainerRef} sx={{ overflowY: "auto", flex: 1 }}>
                {isLoading && <NotificationSkeleton />}
                {!isLoading && (!notifications || notifications.length === 0) && <NotificationEmptyState filter={filter} />}
                {!isLoading && dateGroups.length > 0 && (
                    <List
                        disablePadding
                        sx={{
                            "& .MuiListItem-root:not(:last-child)": {
                                borderBottom: 1,
                                borderColor: "divider",
                            },
                        }}
                    >
                        {dateGroups.map((group) => [
                            <ListSubheader
                                key={`header-${group.label}`}
                                disableSticky
                                sx={{
                                    bgcolor: "transparent",
                                    lineHeight: 2.5,
                                    fontSize: "0.75rem",
                                    fontWeight: "bold",
                                    color: "text.secondary",
                                }}
                            >
                                {t(`date_group_${group.label}`)}
                            </ListSubheader>,
                            ...group.notifications.map((notification) => (
                                <NotificationListItem
                                    key={notification.uuid}
                                    notification={notification}
                                    onClick={onNotificationClick}
                                    onMarkAsRead={onMarkAsRead}
                                />
                            )),
                        ])}
                    </List>
                )}
                {hasMore && <div ref={sentinelRef} />}
                {isLoadingMore && (
                    <Box sx={{ display: "flex", justifyContent: "center", py: 1.5 }}>
                        <CircularProgress size={20} />
                    </Box>
                )}
            </Box>
        </Card>
    );
};
