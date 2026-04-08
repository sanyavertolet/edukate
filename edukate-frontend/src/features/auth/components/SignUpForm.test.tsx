import { render, screen, waitFor } from "@/test/render";
import userEvent from "@testing-library/user-event";
import { server } from "@/test/server";
import { http } from "msw";
import { SignUpForm } from "./SignUpForm";

describe("SignUpForm", () => {
    it("renders the heading and all three input fields", () => {
        render(<SignUpForm />);
        expect(screen.getByRole("heading", { name: /sign up/i })).toBeInTheDocument();
        expect(screen.getByLabelText(/username/i)).toBeInTheDocument();
        expect(screen.getByLabelText(/email/i)).toBeInTheDocument();
        expect(screen.getByLabelText(/password/i)).toBeInTheDocument();
    });

    it("shows username validation error on blur", async () => {
        render(<SignUpForm />);
        await userEvent.click(screen.getByLabelText(/username/i));
        await userEvent.tab();
        expect(await screen.findByText(/username must be between/i)).toBeInTheDocument();
    });

    it("shows email validation error on blur", async () => {
        render(<SignUpForm />);
        await userEvent.type(screen.getByLabelText(/email/i), "not-an-email");
        await userEvent.tab();
        expect(await screen.findByText(/invalid email address/i)).toBeInTheDocument();
    });

    it("shows password validation error on blur", async () => {
        render(<SignUpForm />);
        await userEvent.type(screen.getByLabelText(/password/i), "abc");
        await userEvent.tab();
        expect(await screen.findByText(/password must be between/i)).toBeInTheDocument();
    });

    it("clears the username error once the field becomes valid", async () => {
        render(<SignUpForm />);
        // Trigger an error first
        await userEvent.click(screen.getByLabelText(/username/i));
        await userEvent.tab();
        expect(await screen.findByText(/username must be between/i)).toBeInTheDocument();
        // Fix the value and re-blur
        await userEvent.type(screen.getByLabelText(/username/i), "alice01");
        await userEvent.tab();
        expect(screen.queryByText(/username must be between/i)).not.toBeInTheDocument();
    });

    it("shows all validation errors when submitted with empty fields", async () => {
        render(<SignUpForm />);
        await userEvent.click(screen.getByRole("button", { name: /sign up/i }));
        expect(await screen.findByText(/username must be between/i)).toBeInTheDocument();
        expect(screen.getByText(/invalid email address/i)).toBeInTheDocument();
        expect(screen.getByText(/password must be between/i)).toBeInTheDocument();
    });

    it("disables the submit button while the mutation is pending", async () => {
        server.use(http.post("*/api/v1/auth/sign-up", () => new Promise<never>(() => {})));
        render(<SignUpForm />);
        await userEvent.type(screen.getByLabelText(/username/i), "alice01");
        await userEvent.type(screen.getByLabelText(/email/i), "alice@example.com");
        await userEvent.type(screen.getByLabelText(/password/i), "secret123");
        await userEvent.click(screen.getByRole("button", { name: /sign up/i }));
        expect(screen.getByRole("button", { name: /sign up/i })).toBeDisabled();
    });

    it("calls onSignUpSuccess after successful submission", async () => {
        const onSignUpSuccess = vi.fn();
        render(<SignUpForm onSignUpSuccess={onSignUpSuccess} />);
        await userEvent.type(screen.getByLabelText(/username/i), "alice01");
        await userEvent.type(screen.getByLabelText(/email/i), "alice@example.com");
        await userEvent.type(screen.getByLabelText(/password/i), "secret123");
        await userEvent.click(screen.getByRole("button", { name: /sign up/i }));
        await waitFor(() => expect(onSignUpSuccess).toHaveBeenCalledOnce());
    });
});
