import { http, HttpResponse } from "msw";
import { renderAtPath, screen, waitFor } from "@/test/render";
import { server } from "@/test/server";
import { getGetProblemMockHandler, getGetProblemResponseMock } from "@/generated/backend";
import ProblemPage from "./ProblemPage";

describe("ProblemPage", () => {
    it("renders the heading with the code from the URL", () => {
        renderAtPath("/problems/savchenko/1.1.1", "/problems/:bookSlug/:code", <ProblemPage />);
        expect(screen.getByRole("heading", { name: /problem 1\.1\.1/i })).toBeInTheDocument();
    });

    it("spinner disappears once MSW returns a problem", async () => {
        server.use(getGetProblemMockHandler(getGetProblemResponseMock({ key: "savchenko/1.1.1", bookSlug: "savchenko", code: "1.1.1" })));
        renderAtPath("/problems/savchenko/1.1.1", "/problems/:bookSlug/:code", <ProblemPage />);
        await waitFor(() => {
            expect(screen.queryByRole("progressbar")).not.toBeInTheDocument();
        });
    });

    it("shows 'Problem not found.' alert when the server returns no data", async () => {
        // A 200 with null body sets data=null — triggers the !problem branch in ProblemComponent
        server.use(http.get("*/api/v1/problems/*/*", () => HttpResponse.json(null, { status: 200 })));
        renderAtPath("/problems/savchenko/missing", "/problems/:bookSlug/:code", <ProblemPage />);
        expect(await screen.findByText(/problem not found\./i)).toBeInTheDocument();
    });
});
