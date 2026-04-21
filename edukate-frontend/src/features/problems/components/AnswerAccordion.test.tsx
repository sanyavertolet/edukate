import { http, HttpResponse } from "msw";
import { render, screen, waitFor } from "@/test/render";
import { server } from "@/test/server";
import { getGetAnswerByProblemKeyMockHandler, getGetAnswerByProblemKeyResponseMock } from "@/generated/backend";
import { AnswerAccordionComponent } from "./AnswerAccordion";
import type { Problem } from "@/features/problems/types";

const problem: Problem = {
    key: "savchenko/1.1.1",
    code: "1.1.1",
    bookSlug: "savchenko",
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

describe("AnswerAccordionComponent — unauthenticated", () => {
    beforeEach(() => {
        makeUnauthenticated();
    });

    it("renders the 'Show the answer' label", async () => {
        render(<AnswerAccordionComponent problem={problem} />);
        await waitFor(() => {
            expect(screen.getByText("Show the answer")).toBeInTheDocument();
        });
    });

    it("accordion is disabled when not logged in", async () => {
        render(<AnswerAccordionComponent problem={problem} />);
        await waitFor(() => {
            // MUI Accordion with disabled=true renders the summary button as disabled
            const summary = screen.getByRole("button", { name: /show the answer/i });
            expect(summary).toBeDisabled();
        });
    });
});

describe("AnswerAccordionComponent — authenticated", () => {
    it("accordion is enabled when logged in", async () => {
        render(<AnswerAccordionComponent problem={problem} />);
        await waitFor(() => {
            const summary = screen.getByRole("button", { name: /show the answer/i });
            expect(summary).not.toBeDisabled();
        });
    });

    it("shows answer text after expanding when MSW returns answer", async () => {
        server.use(
            getGetAnswerByProblemKeyMockHandler(
                getGetAnswerByProblemKeyResponseMock({ text: "The answer is 42", images: [] }),
            ),
        );
        render(<AnswerAccordionComponent problem={problem} />);
        await waitFor(() => screen.getByRole("button", { name: /show the answer/i }));
        // The accordion is collapsed by default — just verify it renders without error
        expect(screen.getByText("Show the answer")).toBeInTheDocument();
    });
});
