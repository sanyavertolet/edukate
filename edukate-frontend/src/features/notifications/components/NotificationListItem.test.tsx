import userEvent from "@testing-library/user-event";
import { render, screen } from "@/test/render";
import { NotificationListItem } from "./NotificationListItem";
import type { SimpleNotification, CheckedNotification, InviteNotification } from "@/features/notifications/types";

const makeSimple = (overrides: Partial<SimpleNotification> = {}): SimpleNotification => ({
    uuid: "uuid-1",
    isRead: false,
    createdAt: new Date().toISOString(),
    _type: "simple",
    title: "Welcome",
    message: "Hello, world!",
    source: "system",
    ...overrides,
});

const makeChecked = (overrides: Partial<CheckedNotification> = {}): CheckedNotification => ({
    uuid: "uuid-3",
    isRead: false,
    createdAt: new Date().toISOString(),
    _type: "checked",
    submissionId: 99,
    problemKey: "prob-42",
    status: "SUCCESS",
    ...overrides,
});

const makeInvite = (overrides: Partial<InviteNotification> = {}): InviteNotification => ({
    uuid: "uuid-2",
    isRead: false,
    createdAt: new Date().toISOString(),
    _type: "invite",
    inviterName: "Alice",
    problemSetName: "Math Basics",
    problemSetShareCode: "share-xyz",
    ...overrides,
});

describe("NotificationListItem — simple", () => {
    it("renders title and message", () => {
        render(<NotificationListItem notification={makeSimple()} onClick={vi.fn()} />);
        expect(screen.getByText("Welcome")).toBeInTheDocument();
        expect(screen.getByText(/Hello, world!/)).toBeInTheDocument();
    });

    it("renders source", () => {
        render(<NotificationListItem notification={makeSimple({ source: "edukate-bot" })} onClick={vi.fn()} />);
        expect(screen.getByText("edukate-bot")).toBeInTheDocument();
    });
});

describe("NotificationListItem — checked", () => {
    it("renders 'Submission passed' for SUCCESS", () => {
        render(<NotificationListItem notification={makeChecked({ status: "SUCCESS" })} onClick={vi.fn()} />);
        expect(screen.getByText("Submission passed")).toBeInTheDocument();
    });

    it("renders 'Submission incorrect' for MISTAKE", () => {
        render(<NotificationListItem notification={makeChecked({ status: "MISTAKE" })} onClick={vi.fn()} />);
        expect(screen.getByText("Submission incorrect")).toBeInTheDocument();
    });

    it("renders the problem key", () => {
        render(<NotificationListItem notification={makeChecked({ problemKey: "savchenko/1.1.7" })} onClick={vi.fn()} />);
        expect(screen.getByText(/savchenko\/1\.1\.7/)).toBeInTheDocument();
    });
});

describe("NotificationListItem — invite", () => {
    it("renders inviter name", () => {
        render(<NotificationListItem notification={makeInvite()} onClick={vi.fn()} />);
        expect(screen.getByText(/Alice invites you!/)).toBeInTheDocument();
    });

    it("renders problem set name", () => {
        render(<NotificationListItem notification={makeInvite({ problemSetName: "Physics 101" })} onClick={vi.fn()} />);
        expect(screen.getByText("Physics 101")).toBeInTheDocument();
    });
});

describe("NotificationListItem — read/unread state", () => {
    it("unread notification has bold primary text", () => {
        render(<NotificationListItem notification={makeSimple({ isRead: false })} onClick={vi.fn()} />);
        const primary = screen.getByText("Welcome");
        expect(getComputedStyle(primary).fontWeight).toMatch(/bold|700/);
    });

    it("read notification has normal font weight", () => {
        render(<NotificationListItem notification={makeSimple({ isRead: true })} onClick={vi.fn()} />);
        const primary = screen.getByText("Welcome");
        expect(getComputedStyle(primary).fontWeight).not.toMatch(/bold|700/);
    });
});

describe("NotificationListItem — click", () => {
    it("calls onClick with the notification when clicked", async () => {
        const onClick = vi.fn();
        const notification = makeSimple();
        render(<NotificationListItem notification={notification} onClick={onClick} />);
        await userEvent.click(screen.getByText("Welcome"));
        expect(onClick).toHaveBeenCalledWith(notification);
    });

    it("read notifications are still clickable", async () => {
        const onClick = vi.fn();
        const notification = makeSimple({ isRead: true });
        render(<NotificationListItem notification={notification} onClick={onClick} />);
        await userEvent.click(screen.getByText("Welcome"));
        expect(onClick).toHaveBeenCalledWith(notification);
    });
});

describe("NotificationListItem — mark as read", () => {
    it("shows mark-as-read button for unread notifications", () => {
        render(<NotificationListItem notification={makeSimple()} onClick={vi.fn()} onMarkAsRead={vi.fn()} />);
        expect(screen.getByRole("button", { name: /mark as read/i })).toBeInTheDocument();
    });

    it("does not show mark-as-read button for read notifications", () => {
        render(
            <NotificationListItem notification={makeSimple({ isRead: true })} onClick={vi.fn()} onMarkAsRead={vi.fn()} />,
        );
        expect(screen.queryByRole("button", { name: /mark as read/i })).not.toBeInTheDocument();
    });

    it("calls onMarkAsRead when the button is clicked", async () => {
        const onMarkAsRead = vi.fn();
        const notification = makeSimple();
        render(<NotificationListItem notification={notification} onClick={vi.fn()} onMarkAsRead={onMarkAsRead} />);
        await userEvent.click(screen.getByRole("button", { name: /mark as read/i }));
        expect(onMarkAsRead).toHaveBeenCalledWith(notification);
    });
});
