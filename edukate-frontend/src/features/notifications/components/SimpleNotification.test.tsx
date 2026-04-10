import { render, screen } from "@/test/render";
import { SimpleNotificationComponent } from "./SimpleNotification";
import type { SimpleNotification } from "@/features/notifications/types";

const makeSimple = (overrides: Partial<SimpleNotification> = {}): SimpleNotification => ({
    uuid: "uuid-1",
    isRead: false,
    createdAt: "2024-01-15T10:00:00Z",
    _type: "simple",
    title: "Welcome",
    message: "Hello, world!",
    source: "system",
    ...overrides,
});

describe("SimpleNotificationComponent", () => {
    it("renders title and message", () => {
        render(<SimpleNotificationComponent notification={makeSimple()} />);
        expect(screen.getByText("Welcome")).toBeInTheDocument();
        expect(screen.getByText("Hello, world!")).toBeInTheDocument();
    });

    it("renders the source in the caption", () => {
        render(<SimpleNotificationComponent notification={makeSimple({ source: "edukate-bot" })} />);
        expect(screen.getByText("edukate-bot")).toBeInTheDocument();
    });

    it("renders a formatted date from createdAt", () => {
        render(<SimpleNotificationComponent notification={makeSimple({ createdAt: "2024-06-01T12:00:00Z" })} />);
        // The date is rendered somewhere — just check it's present (formatDate result)
        expect(screen.getByText(/2024/)).toBeInTheDocument();
    });
});
