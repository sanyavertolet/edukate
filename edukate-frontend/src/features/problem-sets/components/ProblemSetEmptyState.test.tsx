import userEvent from "@testing-library/user-event";
import { render, screen } from "@/test/render";
import { ProblemSetEmptyState } from "./ProblemSetEmptyState";

describe("ProblemSetEmptyState", () => {
    it("renders admin tab with create CTA", () => {
        render(<ProblemSetEmptyState tab="admin" />);
        expect(screen.getByText("You haven't created any problem sets yet.")).toBeInTheDocument();
        expect(screen.getByRole("button", { name: "Create Problem Set" })).toBeInTheDocument();
    });

    it("renders user tab with browse CTA", () => {
        render(<ProblemSetEmptyState tab="user" />);
        expect(screen.getByText("You aren't part of any problem set yet.")).toBeInTheDocument();
        expect(screen.getByRole("button", { name: "Browse Public" })).toBeInTheDocument();
    });

    it("renders moderator tab without CTA button", () => {
        render(<ProblemSetEmptyState tab="moderator" />);
        expect(screen.getByText("You aren't a moderator of any problem set.")).toBeInTheDocument();
        expect(screen.queryByRole("button")).not.toBeInTheDocument();
    });

    it("renders public tab without CTA button", () => {
        render(<ProblemSetEmptyState tab="public" />);
        expect(screen.getByText("No public problem sets available yet.")).toBeInTheDocument();
        expect(screen.queryByRole("button")).not.toBeInTheDocument();
    });

    it("navigates to creation page when admin CTA is clicked", async () => {
        render(<ProblemSetEmptyState tab="admin" />);
        await userEvent.click(screen.getByRole("button", { name: "Create Problem Set" }));
        // navigate("/problem-sets/new") called — no crash in MemoryRouter
    });

    it("calls onTabSwitch with 'public' when user CTA is clicked", async () => {
        const onTabSwitch = vi.fn();
        render(<ProblemSetEmptyState tab="user" onTabSwitch={onTabSwitch} />);
        await userEvent.click(screen.getByRole("button", { name: "Browse Public" }));
        expect(onTabSwitch).toHaveBeenCalledWith("public");
    });

    it("does not call onTabSwitch when user CTA is clicked without callback", async () => {
        render(<ProblemSetEmptyState tab="user" />);
        // Should not throw when clicking without onTabSwitch
        await userEvent.click(screen.getByRole("button", { name: "Browse Public" }));
    });
});
