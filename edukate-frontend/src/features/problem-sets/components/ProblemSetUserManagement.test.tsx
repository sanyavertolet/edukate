import userEvent from "@testing-library/user-event";
import { render, screen, waitFor } from "@/test/render";
import { server } from "@/test/server";
import { getGetUserRolesMockHandler, getGetInvitedUsersMockHandler } from "@/generated/backend";
import { ProblemSetUserManagement } from "./ProblemSetUserManagement";
import { UserNameWithRole } from "@/generated/backend";

const members: UserNameWithRole[] = [
    { name: "alice", role: "ADMIN" },
    { name: "bob", role: "MODERATOR" },
    { name: "charlie", role: "USER" },
];

describe("ProblemSetUserManagement", () => {
    it("shows member names after data loads", async () => {
        server.use(getGetUserRolesMockHandler(members), getGetInvitedUsersMockHandler([]));
        render(<ProblemSetUserManagement shareCode="TEST01" />);
        expect(await screen.findByText("alice")).toBeInTheDocument();
        expect(screen.getByText("bob")).toBeInTheDocument();
        expect(screen.getByText("charlie")).toBeInTheDocument();
    });

    it("shows role chips with correct labels", async () => {
        server.use(getGetUserRolesMockHandler(members), getGetInvitedUsersMockHandler([]));
        render(<ProblemSetUserManagement shareCode="TEST01" />);
        expect(await screen.findByText("Admin")).toBeInTheDocument();
        expect(screen.getByText("Moderator")).toBeInTheDocument();
        expect(screen.getByText("User")).toBeInTheDocument();
    });

    it("shows Members section header", async () => {
        server.use(getGetUserRolesMockHandler(members), getGetInvitedUsersMockHandler([]));
        render(<ProblemSetUserManagement shareCode="TEST01" />);
        expect(await screen.findByText("Members")).toBeInTheDocument();
    });

    it("shows 'No members yet' when member list is empty", async () => {
        server.use(getGetUserRolesMockHandler([]), getGetInvitedUsersMockHandler([]));
        render(<ProblemSetUserManagement shareCode="TEST01" />);
        expect(await screen.findByText("No members yet")).toBeInTheDocument();
    });

    it("shows Pending Invitations section when invites exist", async () => {
        server.use(getGetUserRolesMockHandler([]), getGetInvitedUsersMockHandler(["dave", "eve"]));
        render(<ProblemSetUserManagement shareCode="TEST01" />);
        expect(await screen.findByText("Pending Invitations")).toBeInTheDocument();
        expect(screen.getByText("dave")).toBeInTheDocument();
        expect(screen.getByText("eve")).toBeInTheDocument();
    });

    it("hides Pending Invitations section when no invites", async () => {
        server.use(getGetUserRolesMockHandler(members), getGetInvitedUsersMockHandler([]));
        render(<ProblemSetUserManagement shareCode="TEST01" />);
        await screen.findByText("alice");
        expect(screen.queryByText("Pending Invitations")).not.toBeInTheDocument();
    });

    it("opens confirmation dialog when trash icon is clicked for a member", async () => {
        server.use(getGetUserRolesMockHandler(members), getGetInvitedUsersMockHandler([]));
        render(<ProblemSetUserManagement shareCode="TEST01" />);
        await screen.findByText("charlie");

        const removeButtons = screen.getAllByLabelText("remove user");
        await userEvent.click(removeButtons[removeButtons.length - 1]);

        expect(screen.getByText("Remove member")).toBeInTheDocument();
        expect(screen.getByText(/Are you sure you want to remove charlie/)).toBeInTheDocument();
    });

    it("opens confirmation dialog when trash icon is clicked for an invited user", async () => {
        server.use(getGetUserRolesMockHandler([]), getGetInvitedUsersMockHandler(["dave"]));
        render(<ProblemSetUserManagement shareCode="TEST01" />);
        await screen.findByText("dave");

        await userEvent.click(screen.getByLabelText("remove invitation"));

        expect(screen.getByText("Cancel invitation")).toBeInTheDocument();
        expect(screen.getByText(/Are you sure you want to cancel the invitation for dave/)).toBeInTheDocument();
    });

    it("closes confirmation dialog on Cancel click", async () => {
        server.use(getGetUserRolesMockHandler(members), getGetInvitedUsersMockHandler([]));
        render(<ProblemSetUserManagement shareCode="TEST01" />);
        await screen.findByText("charlie");

        const removeButtons = screen.getAllByLabelText("remove user");
        await userEvent.click(removeButtons[removeButtons.length - 1]);
        expect(screen.getByText("Remove member")).toBeInTheDocument();

        await userEvent.click(screen.getByRole("button", { name: "Cancel" }));
        await waitFor(() => {
            expect(screen.queryByText("Remove member")).not.toBeInTheDocument();
        });
    });

    it("shows Pending chip for invited users", async () => {
        server.use(getGetUserRolesMockHandler([]), getGetInvitedUsersMockHandler(["dave"]));
        render(<ProblemSetUserManagement shareCode="TEST01" />);
        expect(await screen.findByText("Pending")).toBeInTheDocument();
    });
});
