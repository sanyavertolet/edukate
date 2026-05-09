import userEvent from "@testing-library/user-event";
import { render, screen } from "@/test/render";
import { SubproblemsComponent } from "./SubproblemsComponent";
import type { Subproblem } from "@/features/problems/types";

const subproblems: Subproblem[] = [
    { code: "a", text: "First subproblem text" },
    { code: "b", text: "Second subproblem text" },
];

describe("SubproblemsComponent", () => {
    it("renders nothing when subproblems is undefined", () => {
        const { container } = render(<SubproblemsComponent />);
        expect(container).toBeEmptyDOMElement();
    });

    it("renders nothing when subproblems is empty", () => {
        const { container } = render(<SubproblemsComponent subproblems={[]} />);
        expect(container).toBeEmptyDOMElement();
    });

    it("renders a tab for each subproblem id", () => {
        render(<SubproblemsComponent subproblems={subproblems} />);
        expect(screen.getByRole("tab", { name: "a" })).toBeInTheDocument();
        expect(screen.getByRole("tab", { name: "b" })).toBeInTheDocument();
    });

    it("shows the first subproblem content by default", () => {
        render(<SubproblemsComponent subproblems={subproblems} />);
        // LazyLatexComponent renders the text eventually — check the tab panel area contains it
        expect(screen.getByRole("tab", { name: "a" })).toHaveAttribute("aria-selected", "true");
    });

    it("switches content when a different tab is clicked", async () => {
        render(<SubproblemsComponent subproblems={subproblems} />);
        await userEvent.click(screen.getByRole("tab", { name: "b" }));
        expect(screen.getByRole("tab", { name: "b" })).toHaveAttribute("aria-selected", "true");
        expect(screen.getByRole("tab", { name: "a" })).toHaveAttribute("aria-selected", "false");
    });
});
