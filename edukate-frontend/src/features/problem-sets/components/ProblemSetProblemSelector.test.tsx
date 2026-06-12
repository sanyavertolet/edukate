import userEvent from "@testing-library/user-event";
import { render, screen } from "@/test/render";
import { ProblemSetProblemSelector, ProblemSetSelection } from "./ProblemSetProblemSelector";
import { ProblemMetadata } from "@/features/problems/types";

const problems: ProblemMetadata[] = [
    {
        key: "savchenko/1.1",
        code: "1.1",
        bookSlug: "savchenko",
        isHard: false,
        tags: [],
        status: "SOLVED",
        createdAt: "2026-01-01T00:00:00Z",
        language: "EN" as const,
    },
    {
        key: "savchenko/1.2",
        code: "1.2",
        bookSlug: "savchenko",
        isHard: true,
        tags: [],
        status: "NOT_SOLVED",
        createdAt: "2026-01-01T00:00:00Z",
        language: "EN" as const,
    },
];

const defaultSelection: ProblemSetSelection = { type: "description" };

describe("ProblemSetProblemSelector", () => {
    it("renders Description item in the sidebar", () => {
        render(
            <ProblemSetProblemSelector
                problems={problems}
                selection={defaultSelection}
                onSelectionChange={vi.fn()}
                isModerator={false}
            />,
        );
        expect(screen.getByText("Description")).toBeInTheDocument();
    });

    it("renders Settings item when user has moderator privileges", () => {
        render(
            <ProblemSetProblemSelector
                problems={problems}
                selection={defaultSelection}
                onSelectionChange={vi.fn()}
                isModerator={true}
            />,
        );
        expect(screen.getByText("Settings")).toBeInTheDocument();
    });

    it("does not render Settings item when user has no moderator privileges", () => {
        render(
            <ProblemSetProblemSelector
                problems={problems}
                selection={defaultSelection}
                onSelectionChange={vi.fn()}
                isModerator={false}
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
                isModerator={false}
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
                isModerator={false}
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
                isModerator={true}
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
                isModerator={false}
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
                isModerator={false}
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
                isModerator={false}
            />,
        );
        const problemButton = screen.getByText("1.1").closest("[role='button']");
        expect(problemButton).toHaveClass("Mui-selected");
    });

    // region order preservation

    function getProblemCodesInOrder(): string[] {
        // The desktop selector renders one ListItemButton per problem with the code as
        // the primary text. Collect them in document order and filter out the static tabs
        // (Description / Settings / Supervisor).
        const codePattern = /^\d+(\.\d+)+$/;
        return screen
            .getAllByRole("button")
            .map((b) => b.textContent ?? "")
            .filter((t) => codePattern.test(t));
    }

    it("renders problems in the exact order received from props (no sort)", () => {
        const unsorted: ProblemMetadata[] = [
            { ...problems[0], key: "savchenko/3.1", code: "3.1" },
            { ...problems[0], key: "savchenko/1.1", code: "1.1" },
            { ...problems[0], key: "savchenko/2.5", code: "2.5" },
        ];
        render(
            <ProblemSetProblemSelector
                problems={unsorted}
                selection={defaultSelection}
                onSelectionChange={vi.fn()}
                isModerator={false}
            />,
        );
        expect(getProblemCodesInOrder()).toEqual(["3.1", "1.1", "2.5"]);
    });

    it("re-renders the navigation in the new order when the problems prop changes", () => {
        const initial: ProblemMetadata[] = [
            { ...problems[0], key: "savchenko/1.1", code: "1.1" },
            { ...problems[0], key: "savchenko/1.2", code: "1.2" },
            { ...problems[0], key: "savchenko/1.3", code: "1.3" },
        ];
        const reordered: ProblemMetadata[] = [initial[2], initial[0], initial[1]];

        const { rerender } = render(
            <ProblemSetProblemSelector
                problems={initial}
                selection={defaultSelection}
                onSelectionChange={vi.fn()}
                isModerator={false}
            />,
        );
        expect(getProblemCodesInOrder()).toEqual(["1.1", "1.2", "1.3"]);

        rerender(
            <ProblemSetProblemSelector
                problems={reordered}
                selection={defaultSelection}
                onSelectionChange={vi.fn()}
                isModerator={false}
            />,
        );
        expect(getProblemCodesInOrder()).toEqual(["1.3", "1.1", "1.2"]);
    });

    // endregion
});
