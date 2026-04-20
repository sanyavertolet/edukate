import userEvent from "@testing-library/user-event";
import { render, screen } from "@/test/render";
import { InvitationDialog } from "./InvitationDialog";

describe("InvitationDialog", () => {
    it("does not render when problemSetInfo is undefined", () => {
        render(<InvitationDialog problemSetInfo={undefined} onClose={vi.fn()} />);
        expect(screen.queryByRole("dialog")).not.toBeInTheDocument();
    });

    it("opens with the problem set name in the title when problemSetInfo is provided", () => {
        render(
            <InvitationDialog problemSetInfo={{ problemSetName: "Math Basics", inviterName: "Alice" }} onClose={vi.fn()} />,
        );
        expect(screen.getByRole("dialog")).toBeInTheDocument();
        expect(screen.getByText(/Math Basics invite/i)).toBeInTheDocument();
    });

    it("shows inviter name and problem set name in the description", () => {
        render(
            <InvitationDialog problemSetInfo={{ problemSetName: "Physics 101", inviterName: "Bob" }} onClose={vi.fn()} />,
        );
        expect(screen.getByText(/Bob has invited you to Physics 101 problem set/i)).toBeInTheDocument();
    });

    it("calls onClose(undefined) when Close is clicked", async () => {
        const onClose = vi.fn();
        render(<InvitationDialog problemSetInfo={{ problemSetName: "Math", inviterName: "Alice" }} onClose={onClose} />);
        await userEvent.click(screen.getByRole("button", { name: /close/i }));
        expect(onClose).toHaveBeenCalledWith(undefined);
    });

    it("calls onClose(false) when Decline is clicked", async () => {
        const onClose = vi.fn();
        render(<InvitationDialog problemSetInfo={{ problemSetName: "Math", inviterName: "Alice" }} onClose={onClose} />);
        await userEvent.click(screen.getByRole("button", { name: /decline/i }));
        expect(onClose).toHaveBeenCalledWith(false);
    });

    it("calls onClose(true) when Accept is clicked", async () => {
        const onClose = vi.fn();
        render(<InvitationDialog problemSetInfo={{ problemSetName: "Math", inviterName: "Alice" }} onClose={onClose} />);
        await userEvent.click(screen.getByRole("button", { name: /accept/i }));
        expect(onClose).toHaveBeenCalledWith(true);
    });
});
