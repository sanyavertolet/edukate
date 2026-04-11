// noinspection DuplicatedCode

import { http, HttpResponse } from "msw";
import userEvent from "@testing-library/user-event";
import { render, screen, waitFor } from "@/test/render";
import { server } from "@/test/server";
import { getGetNotificationsMockHandler, getGetNotificationsResponseMock } from "@/generated/notifier";
import { NotificationButton } from "./NotificationButton";

function makeUnauthenticated() {
    server.use(http.get("*/api/v1/users/whoami", () => HttpResponse.json(null, { status: 401 })));
}

describe("NotificationButton — unauthenticated", () => {
    beforeEach(() => {
        makeUnauthenticated();
    });

    it("does not render the bell button when not logged in", async () => {
        render(<NotificationButton />);
        // Wait a tick for the whoami query to settle
        await new Promise((r) => setTimeout(r, 50));
        expect(screen.queryByRole("button", { name: /show notifications/i })).not.toBeInTheDocument();
    });
});

describe("NotificationButton — authenticated", () => {
    it("renders the bell icon button", async () => {
        server.use(getGetNotificationsMockHandler(getGetNotificationsResponseMock({ statistics: { unread: 0, total: 0 } })));
        render(<NotificationButton />);
        await waitFor(() => {
            expect(screen.getByRole("button", { name: /show notifications/i })).toBeInTheDocument();
        });
    });

    it("shows unread badge count when there are unread notifications", async () => {
        server.use(getGetNotificationsMockHandler(getGetNotificationsResponseMock({ statistics: { unread: 5, total: 10 } })));
        render(<NotificationButton />);
        await waitFor(() => {
            expect(screen.getByText("5")).toBeInTheDocument();
        });
    });

    it("opens the notification menu on button click", async () => {
        server.use(
            getGetNotificationsMockHandler(getGetNotificationsResponseMock({ statistics: { unread: 0, total: 0 } })),
            http.get("*/api/v1/notifications", () => HttpResponse.json({ notifications: [], statistics: { total: 0, unread: 0 } })),
        );
        render(<NotificationButton />);
        const button = await screen.findByRole("button", { name: /show notifications/i });
        await userEvent.click(button);
        await waitFor(() => {
            expect(screen.getByText("Notifications")).toBeInTheDocument();
        });
    });

    it("closes the menu after opening", async () => {
        server.use(
            getGetNotificationsMockHandler(getGetNotificationsResponseMock({ statistics: { unread: 0, total: 0 } })),
            http.get("*/api/v1/notifications", () => HttpResponse.json({ notifications: [], statistics: { total: 0, unread: 0 } })),
        );
        render(<NotificationButton />);
        const button = await screen.findByRole("button", { name: /show notifications/i });
        await userEvent.click(button);
        await waitFor(() => screen.getByText("Notifications"));
        // Close by pressing Escape
        await userEvent.keyboard("{Escape}");
        await waitFor(() => {
            expect(screen.queryByText("Notifications")).not.toBeInTheDocument();
        });
    });
});
