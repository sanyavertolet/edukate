import { render, screen } from "@/test/render";
import { CheckedNotificationComponent } from "./CheckedNotification";
import type { CheckedNotification } from "@/features/notifications/types";

const makeChecked = (overrides: Partial<CheckedNotification> = {}): CheckedNotification => ({
    uuid: "uuid-3",
    isRead: false,
    createdAt: "2024-09-10T15:45:00Z",
    _type: "checked",
    submissionId: "sub-99",
    problemId: "prob-42",
    status: "SUCCESS",
    ...overrides,
});

describe("CheckedNotificationComponent", () => {
    it("renders 'Submission Checked' heading", () => {
        render(<CheckedNotificationComponent notification={makeChecked()} />);
        expect(screen.getByText("Submission Checked")).toBeInTheDocument();
    });

    it("renders the problem ID in the body", () => {
        render(<CheckedNotificationComponent notification={makeChecked({ problemId: "prob-42" })} />);
        expect(screen.getByText(/prob-42/)).toBeInTheDocument();
    });

    it("renders the check status", () => {
        render(<CheckedNotificationComponent notification={makeChecked({ status: "MISTAKE" })} />);
        expect(screen.getByText(/MISTAKE/)).toBeInTheDocument();
    });

    it("renders a formatted date from createdAt", () => {
        render(<CheckedNotificationComponent notification={makeChecked({ createdAt: "2025-01-20T11:00:00Z" })} />);
        expect(screen.getByText(/2025/)).toBeInTheDocument();
    });
});
