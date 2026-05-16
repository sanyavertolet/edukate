import { render, screen, waitFor } from "@/test/render";
import userEvent from "@testing-library/user-event";
import { server } from "@/test/server";
import { getForgotPasswordMockHandler } from "@/generated/gateway";
import { ForgotPasswordForm } from "./ForgotPasswordForm";

describe("ForgotPasswordForm", () => {
    it("renders the title and email field", () => {
        render(<ForgotPasswordForm />);
        expect(screen.getByRole("heading", { name: /reset your password/i })).toBeInTheDocument();
        expect(screen.getByLabelText(/email/i)).toBeInTheDocument();
    });

    it("shows email validation error on submit with invalid email", async () => {
        render(<ForgotPasswordForm />);
        await userEvent.type(screen.getByLabelText(/email/i), "not-an-email");
        await userEvent.click(screen.getByRole("button", { name: /send reset link/i }));
        expect(await screen.findByText(/invalid email address/i)).toBeInTheDocument();
    });

    it("shows confirmation state after successful submission", async () => {
        server.use(getForgotPasswordMockHandler());
        render(<ForgotPasswordForm />);
        await userEvent.type(screen.getByLabelText(/email/i), "alice@example.com");
        await userEvent.click(screen.getByRole("button", { name: /send reset link/i }));
        await waitFor(() => {
            expect(screen.getByText(/check your email/i)).toBeInTheDocument();
        });
    });

    it("calls onBack when back button is clicked", async () => {
        const onBack = vi.fn();
        render(<ForgotPasswordForm onBack={onBack} />);
        await userEvent.click(screen.getByRole("button", { name: /back to sign in/i }));
        expect(onBack).toHaveBeenCalledOnce();
    });

    it("disables submit button while pending", async () => {
        server.use(getForgotPasswordMockHandler(() => new Promise<never>(() => {})));
        render(<ForgotPasswordForm />);
        await userEvent.type(screen.getByLabelText(/email/i), "alice@example.com");
        await userEvent.click(screen.getByRole("button", { name: /send reset link/i }));
        expect(screen.getByRole("button", { name: /send reset link/i })).toBeDisabled();
    });
});
