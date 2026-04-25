import userEvent from "@testing-library/user-event";
import { render, screen } from "@/test/render";
import { ProblemSetProblemSelector, ProblemSetSelection } from "./ProblemSetProblemSelector";
import { ProblemMetadata } from "@/features/problems/types";

const problems: ProblemMetadata[] = [
    { key: "savchenko/1.1", code: "1.1", bookSlug: "savchenko", isHard: false, tags: [], status: "SOLVED" },
    { key: "savchenko/1.2", code: "1.2", bookSlug: "savchenko", isHard: true, tags: [], status: "NOT_SOLVED" },
];

const defaultSelection: ProblemSetSelection = { type: "description" };

describe("ProblemSetProblemSelector", () => {
    it("renders Description item in the sidebar", () => {
        render(
            <ProblemSetProblemSelector
                problems={problems}
                selection={defaultSelection}
                onSelectionChange={vi.fn()}
                isAdmin={false}
            />,
        );
        expect(screen.getByText("Description")).toBeInTheDocument();
    });

    it("renders Settings item when user is admin", () => {
        render(
            <ProblemSetProblemSelector
                problems={problems}
                selection={defaultSelection}
                onSelectionChange={vi.fn()}
                isAdmin={true}
            />,
        );
        expect(screen.getByText("Settings")).toBeInTheDocument();
    });

    it("does not render Settings item when user is not admin", () => {
        render(
            <ProblemSetProblemSelector
                problems={problems}
                selection={defaultSelection}
                onSelectionChange={vi.fn()}
                isAdmin={false}
            />,
        );
        expect(screen.queryByText("Settings")).not.toBeInTheDocument();
    });

    it("renders problem codes in the sidebar", () => {
        render(
            <ProblemSetProblemSelector
                problems={problems}
                selection={defaultSelection}
                onSelectionChange={vi.fn()}
                isAdmin={false}
            />,
        );
        expect(screen.getByText("1.1")).toBeInTheDocument();
        expect(screen.getByText("1.2")).toBeInTheDocument();
    });

    it("calls onSelectionChange with description when Description is clicked", async () => {
        const onSelectionChange = vi.fn();
        render(
            <ProblemSetProblemSelector
                problems={problems}
                selection={{ type: "problem", problem: problems[0] }}
                onSelectionChange={onSelectionChange}
                isAdmin={false}
            />,
        );
        await userEvent.click(screen.getByText("Description"));
        expect(onSelectionChange).toHaveBeenCalledWith({ type: "description" });
    });

    it("calls onSelectionChange with settings when Settings is clicked", async () => {
        const onSelectionChange = vi.fn();
        render(
            <ProblemSetProblemSelector
                problems={problems}
                selection={defaultSelection}
                onSelectionChange={onSelectionChange}
                isAdmin={true}
            />,
        );
        await userEvent.click(screen.getByText("Settings"));
        expect(onSelectionChange).toHaveBeenCalledWith({ type: "settings" });
    });

    it("calls onSelectionChange with problem when a problem is clicked", async () => {
        const onSelectionChange = vi.fn();
        render(
            <ProblemSetProblemSelector
                problems={problems}
                selection={defaultSelection}
                onSelectionChange={onSelectionChange}
                isAdmin={false}
            />,
        );
        await userEvent.click(screen.getByText("1.2"));
        expect(onSelectionChange).toHaveBeenCalledWith({ type: "problem", problem: problems[1] });
    });

    it("highlights the selected Description item", () => {
        render(
            <ProblemSetProblemSelector
                problems={problems}
                selection={{ type: "description" }}
                onSelectionChange={vi.fn()}
                isAdmin={false}
            />,
        );
        const descButton = screen.getByText("Description").closest("[role='button']");
        expect(descButton).toHaveClass("Mui-selected");
    });

    it("highlights the selected problem item", () => {
        render(
            <ProblemSetProblemSelector
                problems={problems}
                selection={{ type: "problem", problem: problems[0] }}
                onSelectionChange={vi.fn()}
                isAdmin={false}
            />,
        );
        const problemButton = screen.getByText("1.1").closest("[role='button']");
        expect(problemButton).toHaveClass("Mui-selected");
    });
});
