import { render, screen } from "@/test/render";
import { server } from "@/test/server";
import { getCountMockHandler, getGetProblemListMockHandler } from "@/generated/backend";
import ProblemListPage from "./ProblemListPage";

describe("ProblemListPage", () => {
    it("renders the heading", () => {
        render(<ProblemListPage />);
        expect(screen.getByRole("heading", { name: /problems/i })).toBeInTheDocument();
    });

    it("renders table column headers", () => {
        render(<ProblemListPage />);
        expect(screen.getByText("Name")).toBeInTheDocument();
        expect(screen.getByText("Tags")).toBeInTheDocument();
    });

    it("renders problem rows returned by the server", async () => {
        server.use(
            getGetProblemListMockHandler([
                {
                    key: "savchenko/problem-smoke",
                    code: "problem-smoke",
                    bookSlug: "savchenko",
                    isHard: false,
                    tags: ["algebra"],
                    status: "NOT_SOLVED",
                },
            ]),
            getCountMockHandler(1),
        );
        render(<ProblemListPage />);
        expect(await screen.findByText("problem-smoke")).toBeInTheDocument();
    });
});
