import userEvent from "@testing-library/user-event";
import { render, screen } from "@/test/render";
import { server } from "@/test/server";
import {
    getGetJoinedProblemSetsMockHandler,
    getGetOwnedProblemSetsMockHandler,
    getGetPublicProblemSetsMockHandler,
} from "@/generated/backend";
import ProblemSetListPage from "./ProblemSetListPage";

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

    it("renders all three tabs", () => {
        render(<ProblemSetListPage />);
        expect(screen.getByRole("tab", { name: /joined/i })).toBeInTheDocument();
        expect(screen.getByRole("tab", { name: /owned/i })).toBeInTheDocument();
        expect(screen.getByRole("tab", { name: /public/i })).toBeInTheDocument();
    });

    it("defaults to Public tab when unauthenticated", () => {
        render(<ProblemSetListPage />);
        expect(screen.getByRole("tab", { name: /public/i })).toHaveAttribute("aria-selected", "true");
    });

    it("renders welcome banner for unauthenticated users", () => {
        localStorage.removeItem("edukate:problem-sets-banner-dismissed");
        render(<ProblemSetListPage />);
        expect(screen.getByText(/problem sets let you organize/i)).toBeInTheDocument();
    });

    it("switches to the Joined tab on click", async () => {
        server.use(...EMPTY_PROBLEM_SET_HANDLERS);
        render(<ProblemSetListPage />);
        await userEvent.click(screen.getByRole("tab", { name: /joined/i }));
        expect(screen.getByRole("tab", { name: /joined/i })).toHaveAttribute("aria-selected", "true");
        expect(screen.getByRole("tab", { name: /public/i })).toHaveAttribute("aria-selected", "false");
    });

    it("switches to the Owned tab on click", async () => {
        server.use(...EMPTY_PROBLEM_SET_HANDLERS);
        render(<ProblemSetListPage />);
        await userEvent.click(screen.getByRole("tab", { name: /owned/i }));
        expect(screen.getByRole("tab", { name: /owned/i })).toHaveAttribute("aria-selected", "true");
    });
});
