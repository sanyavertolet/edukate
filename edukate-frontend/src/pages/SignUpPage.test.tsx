import { useLocation } from "react-router-dom";
import userEvent from "@testing-library/user-event";
import { render, screen } from "@/test/render";
import SignUpPage from "./SignUpPage";

const LocationSpy = () => <span data-testid="pathname">{useLocation().pathname}</span>;

describe("SignUpPage", () => {
    it("renders the sign up heading", () => {
        render(<SignUpPage />);
        expect(screen.getByRole("heading", { name: /sign up/i })).toBeInTheDocument();
    });

    it("navigates to /sign-in when the sign in link is clicked", async () => {
        render(
            <>
                <SignUpPage />
                <LocationSpy />
            </>,
        );
        expect(screen.getByTestId("pathname")).toHaveTextContent("/");
        await userEvent.click(screen.getByText("Sign in"));
        expect(screen.getByTestId("pathname")).toHaveTextContent("/sign-in");
    });
});
