import { render, screen, waitFor } from "@/test/render";
import userEvent from "@testing-library/user-event";
import { server } from "@/test/server";
import { http, HttpResponse } from "msw";
import { getSignUpMockHandler } from "@/generated/gateway";
import { SignUpForm } from "./SignUpForm";

describe("SignUpForm", () => {
    it("renders the heading and all four input fields including confirm password", () => {
        render(<SignUpForm />);
        expect(screen.getByRole("heading", { name: /sign up/i })).toBeInTheDocument();
        expect(screen.getByLabelText(/^username/i)).toBeInTheDocument();
        expect(screen.getByLabelText(/email/i)).toBeInTheDocument();
        expect(screen.getByLabelText(/^password/i)).toBeInTheDocument();
        expect(screen.getByLabelText(/confirm password/i)).toBeInTheDocument();
    });

    it("shows username validation error on blur", async () => {
        render(<SignUpForm />);
        await userEvent.click(screen.getByLabelText(/^username/i));
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
        await userEvent.type(screen.getByLabelText(/^password/i), "abc");
        await userEvent.tab();
        expect(await screen.findByText(/password must be between/i)).toBeInTheDocument();
    });

    it("clears the username error once the field becomes valid", async () => {
        render(<SignUpForm />);
        await userEvent.click(screen.getByLabelText(/^username/i));
        await userEvent.tab();
        expect(await screen.findByText(/username must be between/i)).toBeInTheDocument();
        await userEvent.type(screen.getByLabelText(/^username/i), "alice01");
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

    it("shows do not match error on submit when passwords differ", async () => {
        render(<SignUpForm />);
        await userEvent.type(screen.getByLabelText(/^username/i), "alice01");
        await userEvent.type(screen.getByLabelText(/email/i), "alice@example.com");
        await userEvent.type(screen.getByLabelText(/^password/i), "secret123");
        await userEvent.type(screen.getByLabelText(/confirm password/i), "different");
        await userEvent.click(screen.getByRole("button", { name: /sign up/i }));
        expect(await screen.findByText(/do not match/i)).toBeInTheDocument();
    });

    it("does not call API when passwords do not match", async () => {
        const apiCallSpy = vi.fn();
        server.use(getSignUpMockHandler(apiCallSpy));
        render(<SignUpForm />);
        await userEvent.type(screen.getByLabelText(/^username/i), "alice01");
        await userEvent.type(screen.getByLabelText(/email/i), "alice@example.com");
        await userEvent.type(screen.getByLabelText(/^password/i), "secret123");
        await userEvent.type(screen.getByLabelText(/confirm password/i), "different");
        await userEvent.click(screen.getByRole("button", { name: /sign up/i }));
        expect(await screen.findByText(/do not match/i)).toBeInTheDocument();
        expect(apiCallSpy).not.toHaveBeenCalled();
    });

    it("shows do not match error on blur of confirm field when passwords differ", async () => {
        render(<SignUpForm />);
        await userEvent.type(screen.getByLabelText(/^password/i), "secret123");
        await userEvent.type(screen.getByLabelText(/confirm password/i), "different");
        await userEvent.tab();
        expect(await screen.findByText(/do not match/i)).toBeInTheDocument();
    });

    it("disables the submit button while the mutation is pending", async () => {
        server.use(http.post("*/api/v1/auth/sign-up", () => new Promise<never>(() => {})));
        render(<SignUpForm />);
        await userEvent.type(screen.getByLabelText(/^username/i), "alice01");
        await userEvent.type(screen.getByLabelText(/email/i), "alice@example.com");
        await userEvent.type(screen.getByLabelText(/^password/i), "secret123");
        await userEvent.type(screen.getByLabelText(/confirm password/i), "secret123");
        await userEvent.click(screen.getByRole("button", { name: /sign up/i }));
        expect(screen.getByRole("button", { name: /sign up/i })).toBeDisabled();
    });

    it("shows email verification prompt after successful submission with matching passwords", async () => {
        render(<SignUpForm />);
        await userEvent.type(screen.getByLabelText(/^username/i), "alice01");
        await userEvent.type(screen.getByLabelText(/email/i), "alice@example.com");
        await userEvent.type(screen.getByLabelText(/^password/i), "secret123");
        await userEvent.type(screen.getByLabelText(/confirm password/i), "secret123");
        await userEvent.click(screen.getByRole("button", { name: /sign up/i }));
        await waitFor(() => {
            expect(screen.getByText(/check your email/i)).toBeInTheDocument();
        });
    });

    it("shows already taken error on username field on 409 conflict", async () => {
        server.use(http.post("*/api/v1/auth/sign-up", () => HttpResponse.json(null, { status: 409 })));
        render(<SignUpForm />);
        await userEvent.type(screen.getByLabelText(/^username/i), "alice01");
        await userEvent.type(screen.getByLabelText(/email/i), "alice@example.com");
        await userEvent.type(screen.getByLabelText(/^password/i), "secret123");
        await userEvent.type(screen.getByLabelText(/confirm password/i), "secret123");
        await userEvent.click(screen.getByRole("button", { name: /sign up/i }));
        expect(await screen.findByText(/already taken/i)).toBeInTheDocument();
    });

    it("shows Alert above the form on other API errors", async () => {
        server.use(http.post("*/api/v1/auth/sign-up", () => HttpResponse.json(null, { status: 500 })));
        render(<SignUpForm />);
        await userEvent.type(screen.getByLabelText(/^username/i), "alice01");
        await userEvent.type(screen.getByLabelText(/email/i), "alice@example.com");
        await userEvent.type(screen.getByLabelText(/^password/i), "secret123");
        await userEvent.type(screen.getByLabelText(/confirm password/i), "secret123");
        await userEvent.click(screen.getByRole("button", { name: /sign up/i }));
        expect(await screen.findByText(/failed/i)).toBeInTheDocument();
    });
});
