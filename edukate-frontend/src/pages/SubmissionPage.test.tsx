import { renderAtPath, screen } from "@/test/render";
import { server } from "@/test/server";
import {
    getGetSubmissionByIdMockHandler,
    getGetSubmissionByIdResponseMock,
    getGetCheckResultsBySubmissionIdMockHandler,
} from "@/generated/backend";
import SubmissionPage from "./SubmissionPage";

describe("SubmissionPage", () => {
    it("renders the heading with the id from the URL", () => {
        renderAtPath("/submissions/sub-456", "/submissions/:id", <SubmissionPage />);
        expect(screen.getByRole("heading", { name: /submission sub-456/i })).toBeInTheDocument();
    });

    it("shows Submission Details card when MSW returns a submission", async () => {
        server.use(
            getGetSubmissionByIdMockHandler(getGetSubmissionByIdResponseMock({ id: "sub-456", status: "PENDING" })),
            getGetCheckResultsBySubmissionIdMockHandler([]),
        );
        renderAtPath("/submissions/sub-456", "/submissions/:id", <SubmissionPage />);
        expect(await screen.findByText(/submission details/i)).toBeInTheDocument();
        expect(await screen.findByText(/problem id:/i)).toBeInTheDocument();
    });

    it("shows 'Consider as Solved' button for a PENDING submission", async () => {
        server.use(
            getGetSubmissionByIdMockHandler(getGetSubmissionByIdResponseMock({ id: "sub-456", status: "PENDING" })),
            getGetCheckResultsBySubmissionIdMockHandler([]),
        );
        renderAtPath("/submissions/sub-456", "/submissions/:id", <SubmissionPage />);
        const button = await screen.findByRole("button", { name: /consider as solved/i });
        expect(button).toBeInTheDocument();
        expect(button).not.toBeDisabled();
    });

    it("'Consider as Solved' button is disabled for a SUCCESS submission", async () => {
        server.use(
            getGetSubmissionByIdMockHandler(getGetSubmissionByIdResponseMock({ id: "sub-456", status: "SUCCESS" })),
            getGetCheckResultsBySubmissionIdMockHandler([]),
        );
        renderAtPath("/submissions/sub-456", "/submissions/:id", <SubmissionPage />);
        const button = await screen.findByRole("button", { name: /consider as solved/i });
        expect(button).toBeDisabled();
    });
});
