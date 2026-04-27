import { render, screen } from "@/test/render";
import { NotificationEmptyState } from "./NotificationEmptyState";

describe("NotificationEmptyState", () => {
    it("shows 'No notifications' for 'all' filter", () => {
        render(<NotificationEmptyState filter="all" />);
        expect(screen.getByText("No notifications")).toBeInTheDocument();
    });

    it("shows 'No unread notifications' for 'unread' filter", () => {
        render(<NotificationEmptyState filter="unread" />);
        expect(screen.getByText("No unread notifications")).toBeInTheDocument();
    });

    it("shows 'You're all caught up!' message", () => {
        render(<NotificationEmptyState filter="all" />);
        expect(screen.getByText("You're all caught up!")).toBeInTheDocument();
    });
});
