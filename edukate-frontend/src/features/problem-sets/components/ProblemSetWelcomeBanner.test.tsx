import userEvent from "@testing-library/user-event";
import { render, screen } from "@/test/render";
import { ProblemSetWelcomeBanner } from "./ProblemSetWelcomeBanner";

const DISMISSED_KEY = "edukate:problem-sets-banner-dismissed";

describe("ProblemSetWelcomeBanner", () => {
    beforeEach(() => {
        localStorage.removeItem(DISMISSED_KEY);
    });

    it("renders banner text when not dismissed", () => {
        render(<ProblemSetWelcomeBanner />);
        expect(screen.getByText(/problem sets let you organize/i)).toBeInTheDocument();
    });

    it("hides banner and persists to localStorage on close", async () => {
        render(<ProblemSetWelcomeBanner />);
        await userEvent.click(screen.getByRole("button", { name: /close/i }));
        expect(localStorage.getItem(DISMISSED_KEY)).toBe("true");
    });

    it("does not render banner text when already dismissed", () => {
        localStorage.setItem(DISMISSED_KEY, "true");
        render(<ProblemSetWelcomeBanner />);
        expect(screen.queryByText(/problem sets let you organize/i)).not.toBeInTheDocument();
    });
});
