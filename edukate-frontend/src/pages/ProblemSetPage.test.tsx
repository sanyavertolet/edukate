import { renderAtPath, screen, waitFor } from "@/test/render";
import { server } from "@/test/server";
import { getGetProblemSetByShareCodeMockHandler, getGetProblemSetByShareCodeResponseMock } from "@/generated/backend";
import ProblemSetPage from "./ProblemSetPage";

describe("ProblemSetPage", () => {
    it("renders 'Description' as the selector label before data loads", () => {
        renderAtPath("/problem-sets/test-code", "/problem-sets/:code", <ProblemSetPage />);
        expect(screen.getByText("Description")).toBeInTheDocument();
    });

    it("renders the problem set name as a heading once MSW responds", async () => {
        server.use(
            getGetProblemSetByShareCodeMockHandler(
                getGetProblemSetByShareCodeResponseMock({ name: "Test Problem Set", problems: [] }),
            ),
        );
        renderAtPath("/problem-sets/test-code", "/problem-sets/:code", <ProblemSetPage />);
        await waitFor(() => {
            expect(screen.getByRole("heading", { level: 1, name: "Test Problem Set" })).toBeInTheDocument();
        });
    });
});
