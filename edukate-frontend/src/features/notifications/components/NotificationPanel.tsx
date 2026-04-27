import { FC, SyntheticEvent, useState } from "react";
import {
    Badge,
    Box,
    Button,
    Card,
    CardHeader,
    Divider,
    IconButton,
    List,
    ListSubheader,
    Popover,
    Tab,
    Tabs,
    Tooltip,
} from "@mui/material";
import { ExpandMore, MarkEmailRead } from "@mui/icons-material";
import { frostedGlass } from "@/shared/components/Styled";
import { useGetNotificationsRequest, useMarkAllNotificationsAsReadMutation } from "@/features/notifications/api";
import { BaseNotification } from "@/features/notifications/types";
import { parseDate } from "@/shared/utils/date";
import { NotificationListItem } from "./NotificationListItem";
import { NotificationEmptyState } from "./NotificationEmptyState";
import { NotificationSkeleton } from "./NotificationSkeleton";

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
            label = "Today";
        } else if (ts >= yesterdayStart) {
            label = "Yesterday";
        } else {
            label = "Earlier";
        }

        if (!(label in groups)) {
            groups[label] = [];
            order.push(label);
        }
        groups[label].push(n);
    }

    return order.map((label) => ({ label, notifications: groups[label] }));
}

interface NotificationPanelProps {
    anchorEl?: HTMLElement;
    onClose: () => void;
    onNotificationClick: (notification: BaseNotification) => void;
    onMarkAsRead?: (notification: BaseNotification) => void;
}

export const NotificationPanel: FC<NotificationPanelProps> = ({ anchorEl, onClose, onNotificationClick, onMarkAsRead }) => {
    const [filter, setFilter] = useState<FilterTab>("all");
    const [pageSize, setPageSize] = useState(DEFAULT_PAGE_SIZE);

    const isRead = filter === "unread" ? false : undefined;
    const { data: notificationPage, isLoading } = useGetNotificationsRequest(isRead, pageSize, 0);
    const notifications = notificationPage?.notifications;
    const stats = notificationPage?.statistics;

    const markAllAsReadMutation = useMarkAllNotificationsAsReadMutation();

    const handleTabChange = (_: SyntheticEvent, newValue: FilterTab) => {
        setFilter(newValue);
        setPageSize(DEFAULT_PAGE_SIZE);
    };

    const handleLoadMore = () => {
        setPageSize((prev) => prev + PAGE_INCREMENT);
    };

    const totalForFilter = filter === "all" ? stats?.total : stats?.unread;
    const hasMore = notifications != null && totalForFilter != null && notifications.length < totalForFilter;

    const dateGroups = notifications && notifications.length > 0 ? groupByDate(notifications) : [];

    return (
        <Popover
            open={Boolean(anchorEl)}
            anchorEl={anchorEl}
            onClose={onClose}
            anchorOrigin={{ vertical: "bottom", horizontal: "right" }}
            transformOrigin={{ vertical: "top", horizontal: "right" }}
            slotProps={{
                paper: {
                    sx: (theme) => ({
                        width: { xs: "100vw", sm: 400 },
                        maxWidth: 400,
                        maxHeight: { xs: "80vh", sm: 520 },
                        display: "flex",
                        flexDirection: "column",
                        ...frostedGlass(theme),
                        border: 1,
                        borderColor: "divider",
                    }),
                },
            }}
        >
            <Card
                elevation={0}
                sx={{ display: "flex", flexDirection: "column", overflow: "hidden", height: "100%", bgcolor: "transparent" }}
            >
                <CardHeader
                    title="Notifications"
                    slotProps={{ title: { variant: "subtitle1", fontWeight: "bold" } }}
                    action={
                        <Tooltip title="Mark all as read">
                            <IconButton
                                size="small"
                                onClick={() => {
                                    markAllAsReadMutation.mutate();
                                }}
                                aria-label="Mark all as read"
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
                    <Tab label="All" value="all" />
                    <Tab
                        label={
                            <Badge badgeContent={stats?.unread} color="primary" max={99}>
                                Unread
                            </Badge>
                        }
                        value="unread"
                    />
                </Tabs>
                <Divider />
                <Box sx={{ overflowY: "auto", flex: 1 }}>
                    {isLoading && <NotificationSkeleton />}
                    {!isLoading && (!notifications || notifications.length === 0) && (
                        <NotificationEmptyState filter={filter} />
                    )}
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
                                    {group.label}
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
                </Box>
                {hasMore && (
                    <>
                        <Divider />
                        <Box sx={{ p: 1, display: "flex", justifyContent: "center" }}>
                            <Button size="small" startIcon={<ExpandMore />} onClick={handleLoadMore}>
                                Load more
                            </Button>
                        </Box>
                    </>
                )}
            </Card>
        </Popover>
    );
};
