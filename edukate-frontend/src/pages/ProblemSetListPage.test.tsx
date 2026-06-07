import userEvent from "@testing-library/user-event";
import type { RequestHandler } from "msw";
import { render, screen } from "@/test/render";
import { server } from "@/test/server";
import { getGetMemberProblemSetsMockHandler, getGetPublicProblemSetsMockHandler } from "@/generated/backend";
import ProblemSetListPage from "./ProblemSetListPage";

const EMPTY_PROBLEM_SET_HANDLERS: RequestHandler[] = [
    getGetMemberProblemSetsMockHandler([]),
    getGetPublicProblemSetsMockHandler([]),
];

describe("ProblemSetListPage", () => {
    it("renders the heading", () => {
        render(<ProblemSetListPage />);
        expect(screen.getByRole("heading", { name: /problem sets/i, level: 1 })).toBeInTheDocument();
    });

    it("renders all four tabs", () => {
        render(<ProblemSetListPage />);
        expect(screen.getByRole("tab", { name: /public/i })).toBeInTheDocument();
        expect(screen.getByRole("tab", { name: /^user$/i })).toBeInTheDocument();
        expect(screen.getByRole("tab", { name: /moderator/i })).toBeInTheDocument();
        expect(screen.getByRole("tab", { name: /admin/i })).toBeInTheDocument();
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

    it("switches to the User tab on click", async () => {
        server.use(...EMPTY_PROBLEM_SET_HANDLERS);
        render(<ProblemSetListPage />);
        await userEvent.click(screen.getByRole("tab", { name: /^user$/i }));
        expect(screen.getByRole("tab", { name: /^user$/i })).toHaveAttribute("aria-selected", "true");
        expect(screen.getByRole("tab", { name: /public/i })).toHaveAttribute("aria-selected", "false");
    });

    it("switches to the Admin tab on click", async () => {
        server.use(...EMPTY_PROBLEM_SET_HANDLERS);
        render(<ProblemSetListPage />);
        await userEvent.click(screen.getByRole("tab", { name: /admin/i }));
        expect(screen.getByRole("tab", { name: /admin/i })).toHaveAttribute("aria-selected", "true");
    });
});
