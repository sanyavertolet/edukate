import { render } from "@/test/render";
import { NotificationSkeleton } from "./NotificationSkeleton";

describe("NotificationSkeleton", () => {
    it("renders default 5 skeleton items", () => {
        const { container } = render(<NotificationSkeleton />);
        const items = container.querySelectorAll(".MuiListItem-root");
        expect(items).toHaveLength(5);
    });

    it("renders custom count when provided", () => {
        const { container } = render(<NotificationSkeleton count={3} />);
        const items = container.querySelectorAll(".MuiListItem-root");
        expect(items).toHaveLength(3);
    });
});
