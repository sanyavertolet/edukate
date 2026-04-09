import { http, HttpResponse } from "msw";
import { renderAtPath, screen, waitFor } from "@/test/render";
import { server } from "@/test/server";
import { getGetProblemMockHandler, getGetProblemResponseMock } from "@/generated/backend";
import ProblemPage from "./ProblemPage";

describe("ProblemPage", () => {
    it("renders the heading with the id from the URL", () => {
        renderAtPath("/problems/prob-123", "/problems/:id", <ProblemPage />);
        expect(screen.getByRole("heading", { name: /problem prob-123/i })).toBeInTheDocument();
    });

    it("spinner disappears once MSW returns a problem", async () => {
        server.use(getGetProblemMockHandler(getGetProblemResponseMock({ id: "prob-123" })));
        renderAtPath("/problems/prob-123", "/problems/:id", <ProblemPage />);
        await waitFor(() => {
            expect(screen.queryByRole("progressbar")).not.toBeInTheDocument();
        });
    });

    it("shows 'Problem not found.' alert when the server returns no data", async () => {
        // A 200 with null body sets data=null — triggers the !problem branch in ProblemComponent
        server.use(http.get("*/api/v1/problems/*", () => HttpResponse.json(null, { status: 200 })));
        renderAtPath("/problems/missing-id", "/problems/:id", <ProblemPage />);
        expect(await screen.findByText(/problem not found\./i)).toBeInTheDocument();
    });
});
