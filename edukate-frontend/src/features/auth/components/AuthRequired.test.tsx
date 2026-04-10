import { http, HttpResponse } from "msw";
import userEvent from "@testing-library/user-event";
import { render, screen, waitFor } from "@/test/render";
import { server } from "@/test/server";
import { AuthRequired } from "./AuthRequired";

// Force unauthenticated state by returning null from whoami
function makeUnauthenticated() {
    server.use(http.get("*/api/v1/users/whoami", () => HttpResponse.json(null, { status: 401 })));
}

describe("AuthRequired — bypass", () => {
    it("renders children immediately when bypass=true", () => {
        render(
            <AuthRequired bypass>
                <span>Protected content</span>
            </AuthRequired>,
        );
        expect(screen.getByText("Protected content")).toBeInTheDocument();
    });
});

describe("AuthRequired — unauthenticated", () => {
    beforeEach(() => { makeUnauthenticated(); });

    it("shows 'Authentication Required' heading", async () => {
        render(
            <AuthRequired>
                <span>Secret</span>
            </AuthRequired>,
        );
        await waitFor(() =>
            { expect(screen.getByText("Authentication Required")).toBeInTheDocument(); },
        );
        expect(screen.queryByText("Secret")).not.toBeInTheDocument();
    });

    it("renders sign-in form by default", async () => {
        render(
            <AuthRequired>
                <span>Secret</span>
            </AuthRequired>,
        );
        await waitFor(() =>
            { expect(screen.getByRole("heading", { name: /sign in/i })).toBeInTheDocument(); },
        );
    });

    it("switches to sign-up form when 'Sign up' is clicked", async () => {
        render(
            <AuthRequired>
                <span>Secret</span>
            </AuthRequired>,
        );
        await waitFor(() => screen.getByText("Sign up"));
        await userEvent.click(screen.getByText("Sign up"));
        expect(screen.getByRole("heading", { name: /sign up/i })).toBeInTheDocument();
    });

    it("switches back to sign-in form when 'Sign in' is clicked in sign-up view", async () => {
        render(
            <AuthRequired>
                <span>Secret</span>
            </AuthRequired>,
        );
        await waitFor(() => screen.getByText("Sign up"));
        await userEvent.click(screen.getByText("Sign up"));
        await userEvent.click(screen.getByText("Sign in"));
        expect(screen.getByRole("heading", { name: /sign in/i })).toBeInTheDocument();
    });
});
