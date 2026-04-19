import { render, screen } from "@/test/render";
import { InviteNotificationComponent } from "./InviteNotification";
import type { InviteNotification } from "@/features/notifications/types";

const makeInvite = (overrides: Partial<InviteNotification> = {}): InviteNotification => ({
    uuid: "uuid-2",
    isRead: false,
    createdAt: "2024-03-20T08:30:00Z",
    _type: "invite",
    inviterName: "Alice",
    problemSetName: "Math Basics",
    problemSetShareCode: "share-xyz",
    ...overrides,
});

describe("InviteNotificationComponent", () => {
    it("renders inviter name in the heading", () => {
        render(<InviteNotificationComponent notification={makeInvite()} />);
        expect(screen.getByText(/Alice invites you!/i)).toBeInTheDocument();
    });

    it("renders the problem set name in the body", () => {
        render(<InviteNotificationComponent notification={makeInvite({ problemSetName: "Physics 101" })} />);
        expect(screen.getByText(/Physics 101/)).toBeInTheDocument();
    });

    it("renders a formatted date from createdAt", () => {
        render(<InviteNotificationComponent notification={makeInvite({ createdAt: "2025-11-05T09:00:00Z" })} />);
        expect(screen.getByText(/2025/)).toBeInTheDocument();
    });
});
