import { render, screen, waitFor } from "@/test/render";
import userEvent from "@testing-library/user-event";
import { server } from "@/test/server";
import { getResetPasswordMockHandler } from "@/generated/gateway";
import { ResetPasswordForm } from "./ResetPasswordForm";

const TOKEN = "550e8400-e29b-41d4-a716-446655440000";

describe("ResetPasswordForm", () => {
    it("renders the title and both password fields", () => {
        render(<ResetPasswordForm token={TOKEN} />);
        expect(screen.getByRole("heading", { name: /set new password/i })).toBeInTheDocument();
        expect(screen.getByLabelText(/new password/i)).toBeInTheDocument();
        expect(screen.getByLabelText(/confirm password/i)).toBeInTheDocument();
    });

    it("shows validation error when password is too short", async () => {
        render(<ResetPasswordForm token={TOKEN} />);
        await userEvent.type(screen.getByLabelText(/new password/i), "short");
        await userEvent.click(screen.getByRole("button", { name: /reset password/i }));
        expect(await screen.findByText(/password must be between/i)).toBeInTheDocument();
    });

    it("calls onSuccess after successful reset with matching passwords", async () => {
        server.use(getResetPasswordMockHandler());
        const onSuccess = vi.fn();
        render(<ResetPasswordForm token={TOKEN} onSuccess={onSuccess} />);
        await userEvent.type(screen.getByLabelText(/new password/i), "newpassword123");
        await userEvent.type(screen.getByLabelText(/confirm password/i), "newpassword123");
        await userEvent.click(screen.getByRole("button", { name: /reset password/i }));
        await waitFor(() => {
            expect(onSuccess).toHaveBeenCalledOnce();
        });
    });

    it("disables submit button while pending", async () => {
        server.use(getResetPasswordMockHandler(() => new Promise<never>(() => {})));
        render(<ResetPasswordForm token={TOKEN} />);
        await userEvent.type(screen.getByLabelText(/new password/i), "newpassword123");
        await userEvent.type(screen.getByLabelText(/confirm password/i), "newpassword123");
        await userEvent.click(screen.getByRole("button", { name: /reset password/i }));
        expect(screen.getByRole("button", { name: /reset password/i })).toBeDisabled();
    });

    it("shows do not match error on submit when passwords differ", async () => {
        render(<ResetPasswordForm token={TOKEN} />);
        await userEvent.type(screen.getByLabelText(/new password/i), "newpassword123");
        await userEvent.type(screen.getByLabelText(/confirm password/i), "different456");
        await userEvent.click(screen.getByRole("button", { name: /reset password/i }));
        expect(await screen.findByText(/do not match/i)).toBeInTheDocument();
    });

    it("does not call API when passwords do not match", async () => {
        const apiCallSpy = vi.fn();
        server.use(getResetPasswordMockHandler(apiCallSpy));
        render(<ResetPasswordForm token={TOKEN} />);
        await userEvent.type(screen.getByLabelText(/new password/i), "newpassword123");
        await userEvent.type(screen.getByLabelText(/confirm password/i), "different456");
        await userEvent.click(screen.getByRole("button", { name: /reset password/i }));
        expect(await screen.findByText(/do not match/i)).toBeInTheDocument();
        expect(apiCallSpy).not.toHaveBeenCalled();
    });

    it("shows do not match error on blur of confirm field when passwords differ", async () => {
        render(<ResetPasswordForm token={TOKEN} />);
        await userEvent.type(screen.getByLabelText(/new password/i), "newpassword123");
        await userEvent.type(screen.getByLabelText(/confirm password/i), "different456");
        await userEvent.tab();
        expect(await screen.findByText(/do not match/i)).toBeInTheDocument();
    });
});
