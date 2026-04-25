import userEvent from "@testing-library/user-event";
import { render, screen } from "@/test/render";
import { ProblemSetToolbar } from "./ProblemSetToolbar";

describe("ProblemSetToolbar", () => {
    it("renders join input and create button", () => {
        render(<ProblemSetToolbar />);
        expect(screen.getByPlaceholderText("Join by code")).toBeInTheDocument();
        expect(screen.getByRole("button", { name: /create/i })).toBeInTheDocument();
    });

    it("create button is enabled when not disabled", () => {
        render(<ProblemSetToolbar />);
        expect(screen.getByRole("button", { name: /create/i })).toBeEnabled();
    });

    it("create button is disabled when disabled prop is true", () => {
        render(<ProblemSetToolbar disabled />);
        expect(screen.getByRole("button", { name: /create/i })).toBeDisabled();
    });

    it("navigates to creation page on create click", async () => {
        render(<ProblemSetToolbar />);
        await userEvent.click(screen.getByRole("button", { name: /create/i }));
        // navigate("/problem-sets/new") called — no crash in MemoryRouter
    });

    it("join input is always disabled", () => {
        render(<ProblemSetToolbar />);
        expect(screen.getByPlaceholderText("Join by code")).toBeDisabled();
    });
});
