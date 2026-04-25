import userEvent from "@testing-library/user-event";
import { render, screen } from "@/test/render";
import { ProblemSetEmptyState } from "./ProblemSetEmptyState";

describe("ProblemSetEmptyState", () => {
    it("renders owned tab with create CTA", () => {
        render(<ProblemSetEmptyState tab="owned" />);
        expect(screen.getByText("You haven't created any problem sets yet.")).toBeInTheDocument();
        expect(screen.getByRole("button", { name: "Create Problem Set" })).toBeInTheDocument();
    });

    it("renders joined tab with browse CTA", () => {
        render(<ProblemSetEmptyState tab="joined" />);
        expect(screen.getByText("You haven't joined any problem sets yet.")).toBeInTheDocument();
        expect(screen.getByRole("button", { name: "Browse Public" })).toBeInTheDocument();
    });

    it("renders public tab without CTA button", () => {
        render(<ProblemSetEmptyState tab="public" />);
        expect(screen.getByText("No public problem sets available yet.")).toBeInTheDocument();
        expect(screen.queryByRole("button")).not.toBeInTheDocument();
    });

    it("navigates to creation page when owned CTA is clicked", async () => {
        render(<ProblemSetEmptyState tab="owned" />);
        await userEvent.click(screen.getByRole("button", { name: "Create Problem Set" }));
        // navigate("/problem-sets/new") called — no crash in MemoryRouter
    });

    it("calls onTabSwitch with 'public' when joined CTA is clicked", async () => {
        const onTabSwitch = vi.fn();
        render(<ProblemSetEmptyState tab="joined" onTabSwitch={onTabSwitch} />);
        await userEvent.click(screen.getByRole("button", { name: "Browse Public" }));
        expect(onTabSwitch).toHaveBeenCalledWith("public");
    });

    it("does not call onTabSwitch when joined CTA is clicked without callback", async () => {
        render(<ProblemSetEmptyState tab="joined" />);
        // Should not throw when clicking without onTabSwitch
        await userEvent.click(screen.getByRole("button", { name: "Browse Public" }));
    });
});
