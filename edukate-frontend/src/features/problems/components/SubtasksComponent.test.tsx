import userEvent from "@testing-library/user-event";
import { render, screen } from "@/test/render";
import { SubtasksComponent } from "./SubtasksComponent";
import type { Subtask } from "@/features/problems/types";

const subtasks: Subtask[] = [
    { id: "a", text: "First subtask text" },
    { id: "b", text: "Second subtask text" },
];

describe("SubtasksComponent", () => {
    it("renders nothing when subtasks is undefined", () => {
        const { container } = render(<SubtasksComponent />);
        expect(container).toBeEmptyDOMElement();
    });

    it("renders nothing when subtasks is empty", () => {
        const { container } = render(<SubtasksComponent subtasks={[]} />);
        expect(container).toBeEmptyDOMElement();
    });

    it("renders a tab for each subtask id", () => {
        render(<SubtasksComponent subtasks={subtasks} />);
        expect(screen.getByRole("tab", { name: "a" })).toBeInTheDocument();
        expect(screen.getByRole("tab", { name: "b" })).toBeInTheDocument();
    });

    it("shows the first subtask content by default", () => {
        render(<SubtasksComponent subtasks={subtasks} />);
        // LazyLatexComponent renders the text eventually — check the tab panel area contains it
        expect(screen.getByRole("tab", { name: "a" })).toHaveAttribute("aria-selected", "true");
    });

    it("switches content when a different tab is clicked", async () => {
        render(<SubtasksComponent subtasks={subtasks} />);
        await userEvent.click(screen.getByRole("tab", { name: "b" }));
        expect(screen.getByRole("tab", { name: "b" })).toHaveAttribute("aria-selected", "true");
        expect(screen.getByRole("tab", { name: "a" })).toHaveAttribute("aria-selected", "false");
    });
});
