import userEvent from "@testing-library/user-event";
import { render, screen } from "@/test/render";
import { server } from "@/test/server";
import {
    getGetJoinedProblemSetsMockHandler,
    getGetOwnedProblemSetsMockHandler,
    getGetPublicProblemSetsMockHandler,
} from "@/generated/backend";
import ProblemSetListPage from "./ProblemSetListPage";

// Return empty lists so ProblemSetEmptyList renders instead of ProblemSetCard,
// keeping tab-switch smoke tests focused on navigation behaviour only.
const EMPTY_PROBLEM_SET_HANDLERS = [
    getGetJoinedProblemSetsMockHandler([]),
    getGetOwnedProblemSetsMockHandler([]),
    getGetPublicProblemSetsMockHandler([]),
];

describe("ProblemSetListPage", () => {
    it("renders the heading", () => {
        render(<ProblemSetListPage />);
        expect(screen.getByRole("heading", { name: /problem sets/i, level: 1 })).toBeInTheDocument();
    });

    it("renders all four tabs", () => {
        render(<ProblemSetListPage />);
        expect(screen.getByRole("tab", { name: /info/i })).toBeInTheDocument();
        expect(screen.getByRole("tab", { name: /joined/i })).toBeInTheDocument();
        expect(screen.getByRole("tab", { name: /owned/i })).toBeInTheDocument();
        expect(screen.getByRole("tab", { name: /public/i })).toBeInTheDocument();
    });

    it("shows the info tab content by default", () => {
        render(<ProblemSetListPage />);
        expect(screen.getByRole("tab", { name: /info/i })).toHaveAttribute("aria-selected", "true");
        // ProblemSetInfoCards unique static text
        expect(screen.getByText(/hit the road/i)).toBeInTheDocument();
    });

    it("switches to the Joined tab on click", async () => {
        server.use(...EMPTY_PROBLEM_SET_HANDLERS);
        render(<ProblemSetListPage />);
        await userEvent.click(screen.getByRole("tab", { name: /joined/i }));
        expect(screen.getByRole("tab", { name: /joined/i })).toHaveAttribute("aria-selected", "true");
        expect(screen.getByRole("tab", { name: /info/i })).toHaveAttribute("aria-selected", "false");
    });

    it("switches to the Public tab on click", async () => {
        server.use(...EMPTY_PROBLEM_SET_HANDLERS);
        render(<ProblemSetListPage />);
        await userEvent.click(screen.getByRole("tab", { name: /public/i }));
        expect(screen.getByRole("tab", { name: /public/i })).toHaveAttribute("aria-selected", "true");
    });
});
