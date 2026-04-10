import { render, screen, waitFor } from "@/test/render";
import userEvent from "@testing-library/user-event";
import { server } from "@/test/server";
import { http } from "msw";
import { SignInForm } from "./SignInForm";

describe("SignInForm", () => {
    it("renders the heading and both input fields", () => {
        render(<SignInForm />);
        expect(screen.getByRole("heading", { name: /sign in/i })).toBeInTheDocument();
        expect(screen.getByLabelText(/username/i)).toBeInTheDocument();
        expect(screen.getByLabelText(/password/i)).toBeInTheDocument();
    });

    it("shows username required error on blur when empty", async () => {
        render(<SignInForm />);
        await userEvent.click(screen.getByLabelText(/username/i));
        await userEvent.tab();
        expect(await screen.findByText("Please enter your username.")).toBeInTheDocument();
    });

    it("shows password required error on blur when empty", async () => {
        render(<SignInForm />);
        await userEvent.click(screen.getByLabelText(/password/i));
        await userEvent.tab();
        expect(await screen.findByText("Please enter your password.")).toBeInTheDocument();
    });

    it("shows both validation errors when submitted with empty fields", async () => {
        render(<SignInForm />);
        await userEvent.click(screen.getByRole("button", { name: /sign in/i }));
        expect(await screen.findByText("Please enter your username.")).toBeInTheDocument();
        expect(screen.getByText("Please enter your password.")).toBeInTheDocument();
    });

    it("disables the submit button while the mutation is pending", async () => {
        server.use(http.post("*/api/v1/auth/sign-in", () => new Promise<never>(() => {})));
        render(<SignInForm />);
        await userEvent.type(screen.getByLabelText(/username/i), "alice");
        await userEvent.type(screen.getByLabelText(/password/i), "secret");
        await userEvent.click(screen.getByRole("button", { name: /sign in/i }));
        expect(screen.getByRole("button", { name: /sign in/i })).toBeDisabled();
    });

    it("calls onSignInSuccess after successful credentials submission", async () => {
        const onSignInSuccess = vi.fn();
        render(<SignInForm onSignInSuccess={onSignInSuccess} />);
        await userEvent.type(screen.getByLabelText(/username/i), "alice");
        await userEvent.type(screen.getByLabelText(/password/i), "secret");
        await userEvent.click(screen.getByRole("button", { name: /sign in/i }));
        await waitFor(() => { expect(onSignInSuccess).toHaveBeenCalledOnce(); });
    });
});
