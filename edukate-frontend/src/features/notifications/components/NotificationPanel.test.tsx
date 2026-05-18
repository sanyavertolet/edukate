import userEvent from "@testing-library/user-event";
import { render, screen, waitFor } from "@/test/render";
import { server } from "@/test/server";
import { getGetNotificationsMockHandler } from "@/generated/notifier";
import { NotificationPanel } from "./NotificationPanel";
import type { SimpleNotification, InviteNotification, CheckedNotification } from "@/features/notifications/types";

const simpleNotification: SimpleNotification = {
    uuid: "n-simple",
    isRead: false,
    createdAt: new Date().toISOString(),
    _type: "simple",
    title: "System Update",
    message: "New features are live.",
    source: "platform",
};

const inviteNotification: InviteNotification = {
    uuid: "n-invite",
    isRead: false,
    createdAt: new Date().toISOString(),
    _type: "invite",
    inviterName: "Alice",
    problemSetName: "Math Pack",
    problemSetShareCode: "math-001",
};

const checkedNotification: CheckedNotification = {
    uuid: "n-checked",
    isRead: false,
    createdAt: new Date().toISOString(),
    _type: "checked",
    submissionId: 1,
    problemKey: "savchenko/1.1.7",
    status: "SUCCESS",
};

const readNotification: SimpleNotification = {
    ...simpleNotification,
    uuid: "n-read",
    isRead: true,
    title: "Already Read",
};

function renderPanel(anchorEl: HTMLElement | undefined = document.createElement("button")) {
    return render(<NotificationPanel anchorEl={anchorEl} onClose={vi.fn()} onNotificationClick={vi.fn()} />);
}

describe("NotificationPanel — header", () => {
    it("renders 'Notifications' heading and 'Mark all as read' button", () => {
        renderPanel();
        expect(screen.getByText("Notifications")).toBeInTheDocument();
        expect(screen.getByRole("button", { name: /mark all as read/i })).toBeInTheDocument();
    });
});

describe("NotificationPanel — tabs", () => {
    it("defaults to 'All' tab selected", () => {
        renderPanel();
        expect(screen.getByRole("tab", { name: /all/i, selected: true })).toBeInTheDocument();
    });

    it("can switch to 'Unread' tab", async () => {
        server.use(getGetNotificationsMockHandler({ notifications: [], statistics: { total: 0, unread: 0 } }));
        renderPanel();
        await userEvent.click(screen.getByRole("tab", { name: /unread/i }));
        expect(screen.getByRole("tab", { name: /unread/i, selected: true })).toBeInTheDocument();
    });
});

describe("NotificationPanel — notification types", () => {
    it("renders a simple notification", async () => {
        server.use(
            getGetNotificationsMockHandler({ notifications: [simpleNotification], statistics: { total: 1, unread: 1 } }),
        );
        renderPanel();
        await waitFor(() => {
            expect(screen.getByText("System Update")).toBeInTheDocument();
        });
        expect(screen.getByText(/New features are live\./)).toBeInTheDocument();
    });

    it("renders an invite notification", async () => {
        server.use(
            getGetNotificationsMockHandler({ notifications: [inviteNotification], statistics: { total: 1, unread: 1 } }),
        );
        renderPanel();
        await waitFor(() => {
            expect(screen.getByText(/Alice invites you!/)).toBeInTheDocument();
        });
        expect(screen.getByText("Math Pack")).toBeInTheDocument();
    });

    it("renders a checked notification with status-aware title", async () => {
        server.use(
            getGetNotificationsMockHandler({ notifications: [checkedNotification], statistics: { total: 1, unread: 1 } }),
        );
        renderPanel();
        await waitFor(() => {
            expect(screen.getByText("Submission passed")).toBeInTheDocument();
        });
        expect(screen.getByText(/savchenko\/1\.1\.7/)).toBeInTheDocument();
    });

    it("renders multiple mixed notifications", async () => {
        server.use(
            getGetNotificationsMockHandler({
                notifications: [simpleNotification, inviteNotification, checkedNotification],
                statistics: { total: 3, unread: 3 },
            }),
        );
        renderPanel();
        await waitFor(() => {
            expect(screen.getByText("System Update")).toBeInTheDocument();
        });
        expect(screen.getByText(/Alice invites you!/)).toBeInTheDocument();
        expect(screen.getByText("Submission passed")).toBeInTheDocument();
    });
});

describe("NotificationPanel — read state", () => {
    it("read notifications are still clickable (not disabled)", async () => {
        server.use(
            getGetNotificationsMockHandler({ notifications: [readNotification], statistics: { total: 1, unread: 0 } }),
        );
        const onNotificationClick = vi.fn();
        render(
            <NotificationPanel
                anchorEl={document.createElement("button")}
                onClose={vi.fn()}
                onNotificationClick={onNotificationClick}
            />,
        );
        await waitFor(() => {
            expect(screen.getByText("Already Read")).toBeInTheDocument();
        });
        await userEvent.click(screen.getByText("Already Read"));
        expect(onNotificationClick).toHaveBeenCalled();
    });
});

describe("NotificationPanel — empty state", () => {
    it("shows empty state when no notifications", async () => {
        server.use(getGetNotificationsMockHandler({ notifications: [], statistics: { total: 0, unread: 0 } }));
        renderPanel();
        await waitFor(() => {
            expect(screen.getByText("No notifications")).toBeInTheDocument();
        });
        expect(screen.getByText("You're all caught up!")).toBeInTheDocument();
    });
});

describe("NotificationPanel — load more", () => {
    it("does not show a 'Load more' button (uses infinite scroll instead)", async () => {
        server.use(
            getGetNotificationsMockHandler({
                notifications: [simpleNotification],
                statistics: { total: 25, unread: 3 },
            }),
        );
        renderPanel();
        await waitFor(() => {
            expect(screen.getByText("System Update")).toBeInTheDocument();
        });
        expect(screen.queryByRole("button", { name: /load more/i })).not.toBeInTheDocument();
    });
});

describe("NotificationPanel — closed state", () => {
    it("does not render when anchorEl is undefined", () => {
        render(<NotificationPanel anchorEl={undefined} onClose={vi.fn()} onNotificationClick={vi.fn()} />);
        expect(screen.queryByText("Notifications")).not.toBeInTheDocument();
    });
});

describe("NotificationPanel — date group headers", () => {
    it("shows 'Today' header for notifications created today", async () => {
        server.use(
            getGetNotificationsMockHandler({ notifications: [simpleNotification], statistics: { total: 1, unread: 1 } }),
        );
        renderPanel();
        await waitFor(() => {
            expect(screen.getByText("Today")).toBeInTheDocument();
        });
    });
});

describe("NotificationPanel — unread badge on tab", () => {
    it("shows unread count badge on the Unread tab", async () => {
        server.use(
            getGetNotificationsMockHandler({ notifications: [simpleNotification], statistics: { total: 1, unread: 5 } }),
        );
        renderPanel();
        await waitFor(() => {
            expect(screen.getByText("5")).toBeInTheDocument();
        });
    });
});

describe("NotificationPanel — onNotificationClick", () => {
    it("calls onNotificationClick with the notification when an item is clicked", async () => {
        server.use(
            getGetNotificationsMockHandler({ notifications: [simpleNotification], statistics: { total: 1, unread: 1 } }),
        );
        const onNotificationClick = vi.fn();
        render(
            <NotificationPanel
                anchorEl={document.createElement("button")}
                onClose={vi.fn()}
                onNotificationClick={onNotificationClick}
            />,
        );
        await waitFor(() => screen.getByText("System Update"));
        await userEvent.click(screen.getByText("System Update"));
        expect(onNotificationClick).toHaveBeenCalledWith(simpleNotification);
    });
});
