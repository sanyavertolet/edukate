import userEvent from "@testing-library/user-event";
import { render, screen } from "@/test/render";
import { ProblemSetListItem } from "./ProblemSetListItem";
import { ProblemSetMetadata } from "@/features/problem-sets/types";

const baseMeta: ProblemSetMetadata = {
    name: "Mechanics 101",
    description: "A collection of mechanics problems",
    admins: ["alice", "bob"],
    shareCode: "abc123",
    isPublic: true,
    size: 12,
    solvedCount: 5,
};

describe("ProblemSetListItem", () => {
    it("renders name and description", () => {
        render(<ProblemSetListItem problemSetMetadata={baseMeta} />);
        expect(screen.getByText("Mechanics 101")).toBeInTheDocument();
        expect(screen.getByText("A collection of mechanics problems")).toBeInTheDocument();
    });

    it("shows fallback when description is missing", () => {
        render(<ProblemSetListItem problemSetMetadata={{ ...baseMeta, description: undefined }} />);
        expect(screen.getByText("No description")).toBeInTheDocument();
    });

    it("shows problem count when unauthenticated", () => {
        render(<ProblemSetListItem problemSetMetadata={{ ...baseMeta, solvedCount: 0 }} />);
        expect(screen.getByText("12 problems")).toBeInTheDocument();
    });

    it("shows admin label with multiple admins", () => {
        render(<ProblemSetListItem problemSetMetadata={baseMeta} />);
        expect(screen.getByText("by alice (+1)")).toBeInTheDocument();
    });

    it("shows admin label with single admin", () => {
        render(<ProblemSetListItem problemSetMetadata={{ ...baseMeta, admins: ["alice"] }} />);
        expect(screen.getByText("by alice")).toBeInTheDocument();
    });

    it("does not show admin label when admins is empty", () => {
        render(<ProblemSetListItem problemSetMetadata={{ ...baseMeta, admins: [] }} />);
        expect(screen.queryByText(/^by /)).not.toBeInTheDocument();
    });

    it("shows public icon for public sets", () => {
        render(<ProblemSetListItem problemSetMetadata={{ ...baseMeta, isPublic: true }} />);
        expect(screen.getByTestId("PublicIcon")).toBeInTheDocument();
    });

    it("shows lock icon for private sets", () => {
        render(<ProblemSetListItem problemSetMetadata={{ ...baseMeta, isPublic: false }} />);
        expect(screen.getByTestId("LockIcon")).toBeInTheDocument();
    });

    it("navigates on click without crashing", async () => {
        render(<ProblemSetListItem problemSetMetadata={baseMeta} />);
        await userEvent.click(screen.getByText("Mechanics 101"));
    });
});
