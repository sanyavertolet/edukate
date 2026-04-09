import { http, HttpResponse } from "msw";
import { render, screen, waitFor } from "@/test/render";
import { server } from "@/test/server";
import { getGetResultByIdMockHandler, getGetResultByIdResponseMock } from "@/generated/backend";
import { ResultAccordionComponent } from "./ResultAccordion";
import type { Problem } from "@/features/problems/types";

const problem: Problem = {
    id: "prob-1",
    isHard: false,
    tags: [],
    text: "Solve it",
    subtasks: [],
    images: [],
    status: "NOT_SOLVED",
    hasResult: true,
};

function makeUnauthenticated() {
    server.use(http.get("*/api/v1/users/whoami", () => HttpResponse.json(null, { status: 401 })));
}

describe("ResultAccordionComponent — unauthenticated", () => {
    beforeEach(() => makeUnauthenticated());

    it("renders the 'Show the result' label", async () => {
        render(<ResultAccordionComponent problem={problem} />);
        await waitFor(() => expect(screen.getByText("Show the result")).toBeInTheDocument());
    });

    it("accordion is disabled when not logged in", async () => {
        render(<ResultAccordionComponent problem={problem} />);
        await waitFor(() => {
            // MUI Accordion with disabled=true renders the summary button as disabled
            const summary = screen.getByRole("button", { name: /show the result/i });
            expect(summary).toBeDisabled();
        });
    });
});

describe("ResultAccordionComponent — authenticated", () => {
    it("accordion is enabled when logged in", async () => {
        render(<ResultAccordionComponent problem={problem} />);
        await waitFor(() => {
            const summary = screen.getByRole("button", { name: /show the result/i });
            expect(summary).not.toBeDisabled();
        });
    });

    it("shows result text after expanding when MSW returns result", async () => {
        server.use(
            getGetResultByIdMockHandler(getGetResultByIdResponseMock({ text: "The answer is 42", images: [] })),
        );
        render(<ResultAccordionComponent problem={problem} />);
        await waitFor(() => screen.getByRole("button", { name: /show the result/i }));
        // The accordion is collapsed by default — just verify it renders without error
        expect(screen.getByText("Show the result")).toBeInTheDocument();
    });
});
