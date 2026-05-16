import { render, screen, waitFor } from "@/test/render";
import userEvent from "@testing-library/user-event";
import { server } from "@/test/server";
import { http, HttpResponse } from "msw";
import { SignInForm } from "./SignInForm";

describe("SignInForm", () => {
    it("renders the heading and both input fields", () => {
        render(<SignInForm />);
        expect(screen.getByRole("heading", { name: /sign in/i })).toBeInTheDocument();
        expect(screen.getByLabelText(/username or email/i)).toBeInTheDocument();
        expect(screen.getByLabelText(/^password/i)).toBeInTheDocument();
    });

    it("shows login required error on blur when empty", async () => {
        render(<SignInForm />);
        await userEvent.click(screen.getByLabelText(/username or email/i));
        await userEvent.tab();
        expect(await screen.findByText("Please enter your username or email.")).toBeInTheDocument();
    });

    it("shows password required error on blur when empty", async () => {
        render(<SignInForm />);
        await userEvent.click(screen.getByLabelText(/^password/i));
        await userEvent.tab();
        expect(await screen.findByText("Please enter your password.")).toBeInTheDocument();
    });

    it("shows both validation errors when submitted with empty fields", async () => {
        render(<SignInForm />);
        await userEvent.click(screen.getByRole("button", { name: /sign in/i }));
        expect(await screen.findByText("Please enter your username or email.")).toBeInTheDocument();
        expect(screen.getByText("Please enter your password.")).toBeInTheDocument();
    });

    it("disables the submit button while the mutation is pending", async () => {
        server.use(http.post("*/api/v1/auth/sign-in", () => new Promise<never>(() => {})));
        render(<SignInForm />);
        await userEvent.type(screen.getByLabelText(/username or email/i), "alice");
        await userEvent.type(screen.getByLabelText(/^password/i), "secret");
        await userEvent.click(screen.getByRole("button", { name: /sign in/i }));
        expect(screen.getByRole("button", { name: /sign in/i })).toBeDisabled();
    });

    it("renders forgot password button and calls onForgotPassword", async () => {
        const onForgotPassword = vi.fn();
        render(<SignInForm onForgotPassword={onForgotPassword} />);
        await userEvent.click(screen.getByRole("button", { name: /forgot password/i }));
        expect(onForgotPassword).toHaveBeenCalledOnce();
    });

    it("calls onSignInSuccess after successful credentials submission", async () => {
        const onSignInSuccess = vi.fn();
        render(<SignInForm onSignInSuccess={onSignInSuccess} />);
        await userEvent.type(screen.getByLabelText(/username or email/i), "alice");
        await userEvent.type(screen.getByLabelText(/^password/i), "secret");
        await userEvent.click(screen.getByRole("button", { name: /sign in/i }));
        await waitFor(() => {
            expect(onSignInSuccess).toHaveBeenCalledOnce();
        });
    });

    it("shows invalid credentials Alert on 403 response", async () => {
        server.use(http.post("*/api/v1/auth/sign-in", () => HttpResponse.json(null, { status: 403 })));
        render(<SignInForm />);
        await userEvent.type(screen.getByLabelText(/username or email/i), "alice");
        await userEvent.type(screen.getByLabelText(/^password/i), "wrongpassword");
        await userEvent.click(screen.getByRole("button", { name: /sign in/i }));
        expect(await screen.findByText(/invalid username or password/i)).toBeInTheDocument();
    });

    it("shows not verified Alert on 423 response", async () => {
        server.use(http.post("*/api/v1/auth/sign-in", () => HttpResponse.json(null, { status: 423 })));
        render(<SignInForm />);
        await userEvent.type(screen.getByLabelText(/username or email/i), "alice");
        await userEvent.type(screen.getByLabelText(/^password/i), "secret");
        await userEvent.click(screen.getByRole("button", { name: /sign in/i }));
        expect(await screen.findByText(/not verified/i)).toBeInTheDocument();
    });

    it("clears error Alert on next submission attempt", async () => {
        server.use(http.post("*/api/v1/auth/sign-in", () => HttpResponse.json(null, { status: 403 })));
        render(<SignInForm />);
        await userEvent.type(screen.getByLabelText(/username or email/i), "alice");
        await userEvent.type(screen.getByLabelText(/^password/i), "wrong");
        await userEvent.click(screen.getByRole("button", { name: /sign in/i }));
        expect(await screen.findByText(/invalid username or password/i)).toBeInTheDocument();

        server.use(http.post("*/api/v1/auth/sign-in", () => new Promise<never>(() => {})));
        await userEvent.click(screen.getByRole("button", { name: /sign in/i }));
        expect(screen.queryByText(/invalid username or password/i)).not.toBeInTheDocument();
    });
});
