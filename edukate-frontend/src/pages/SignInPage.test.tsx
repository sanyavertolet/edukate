import { useLocation } from "react-router-dom";
import userEvent from "@testing-library/user-event";
import { render, screen } from "@/test/render";
import SignInPage from "./SignInPage";

const LocationSpy = () => <span data-testid="pathname">{useLocation().pathname}</span>;

describe("SignInPage", () => {
    it("renders the sign in heading", () => {
        render(<SignInPage />);
        expect(screen.getByRole("heading", { name: /sign in/i })).toBeInTheDocument();
    });

    it("navigates to /sign-up when the sign up link is clicked", async () => {
        render(
            <>
                <SignInPage />
                <LocationSpy />
            </>,
        );
        expect(screen.getByTestId("pathname")).toHaveTextContent("/");
        await userEvent.click(screen.getByText("Sign up"));
        expect(screen.getByTestId("pathname")).toHaveTextContent("/sign-up");
    });
});
