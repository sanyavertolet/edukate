import { useLocation } from "react-router-dom";
import userEvent from "@testing-library/user-event";
import { render, screen } from "@/test/render";
import IndexPage from "./IndexPage";

const LocationSpy = () => <span data-testid="pathname">{useLocation().pathname}</span>;

describe("IndexPage", () => {
    it("renders the heading and CTA button", () => {
        render(<IndexPage />);
        expect(screen.getByRole("heading", { name: /welcome to edukate/i })).toBeInTheDocument();
        expect(screen.getByRole("button", { name: /explore problems/i })).toBeInTheDocument();
    });

    it("navigates to /problems when the CTA button is clicked", async () => {
        render(
            <>
                <IndexPage />
                <LocationSpy />
            </>,
        );
        expect(screen.getByTestId("pathname")).toHaveTextContent("/");
        await userEvent.click(screen.getByRole("button", { name: /explore problems/i }));
        expect(screen.getByTestId("pathname")).toHaveTextContent("/problems");
    });
});
