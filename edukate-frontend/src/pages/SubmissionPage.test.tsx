import userEvent from "@testing-library/user-event";
import { renderAtPath, screen } from "@/test/render";
import { server } from "@/test/server";
import {
    getGetSubmissionByIdMockHandler,
    getGetSubmissionByIdResponseMock,
    getGetCheckResultsBySubmissionIdMockHandler,
    getWhoamiMockHandler,
} from "@/generated/backend";
import SubmissionPage from "./SubmissionPage";

function mockSubmission(overrides: { id?: number; status?: "PENDING" | "SUCCESS" | "FAILED" } = {}) {
    return getGetSubmissionByIdResponseMock({ id: 456, status: "PENDING", ...overrides });
}

describe("SubmissionPage", () => {
    it("renders the heading with the id from the URL", () => {
        renderAtPath("/submissions/sub-456", "/submissions/:id", <SubmissionPage />);
        expect(screen.getByRole("heading", { name: /submission #sub-456/i })).toBeInTheDocument();
    });

    it("shows submission data when MSW returns a submission", async () => {
        server.use(
            getGetSubmissionByIdMockHandler(mockSubmission({ status: "PENDING" })),
            getGetCheckResultsBySubmissionIdMockHandler([]),
        );
        renderAtPath("/submissions/sub-456", "/submissions/:id", <SubmissionPage />);
        expect(await screen.findByText(/pending review/i)).toBeInTheDocument();
        expect(await screen.findByText(/^problem$/i)).toBeInTheDocument();
    });
});

describe("SubmissionPage — ActionsSection", () => {
    it("renders the supervisor-check icon for a PENDING submission", async () => {
        server.use(
            getGetSubmissionByIdMockHandler(mockSubmission({ status: "PENDING" })),
            getGetCheckResultsBySubmissionIdMockHandler([]),
        );
        renderAtPath("/submissions/sub-456", "/submissions/:id", <SubmissionPage />);
        expect(await screen.findByRole("button", { name: /request supervisor review/i })).toBeInTheDocument();
    });

    it("renders the self-check icon for a PENDING submission", async () => {
        server.use(
            getGetSubmissionByIdMockHandler(mockSubmission({ status: "PENDING" })),
            getGetCheckResultsBySubmissionIdMockHandler([]),
        );
        renderAtPath("/submissions/sub-456", "/submissions/:id", <SubmissionPage />);
        expect(await screen.findByRole("button", { name: /mark as solved/i })).toBeInTheDocument();
    });

    it("hides the self-check icon for a SUCCESS submission", async () => {
        server.use(
            getGetSubmissionByIdMockHandler(mockSubmission({ status: "SUCCESS" })),
            getGetCheckResultsBySubmissionIdMockHandler([]),
        );
        renderAtPath("/submissions/sub-456", "/submissions/:id", <SubmissionPage />);
        expect(await screen.findByText(/^success$/i)).toBeInTheDocument();
        expect(screen.queryByRole("button", { name: /mark as solved/i })).not.toBeInTheDocument();
    });

    it("hides the smart-check icon for a non-moderator user", async () => {
        server.use(
            getGetSubmissionByIdMockHandler(mockSubmission({ status: "PENDING" })),
            getGetCheckResultsBySubmissionIdMockHandler([]),
        );
        renderAtPath("/submissions/sub-456", "/submissions/:id", <SubmissionPage />);
        await screen.findByRole("button", { name: /request supervisor review/i });
        expect(screen.queryByRole("button", { name: /request smart check/i })).not.toBeInTheDocument();
    });

    it("shows the smart-check icon for a moderator user", async () => {
        server.use(
            getWhoamiMockHandler({ name: "mod", email: "m@m.io", roles: ["MODERATOR"], status: "ACTIVE" }),
            getGetSubmissionByIdMockHandler(mockSubmission({ status: "PENDING" })),
            getGetCheckResultsBySubmissionIdMockHandler([]),
        );
        renderAtPath("/submissions/sub-456", "/submissions/:id", <SubmissionPage />);
        expect(await screen.findByRole("button", { name: /request smart check/i })).toBeInTheDocument();
    });

    it("opens the supervisor-check dialog when the icon is clicked", async () => {
        server.use(
            getGetSubmissionByIdMockHandler(mockSubmission({ status: "PENDING" })),
            getGetCheckResultsBySubmissionIdMockHandler([]),
        );
        renderAtPath("/submissions/sub-456", "/submissions/:id", <SubmissionPage />);
        const supervisorBtn = await screen.findByRole("button", { name: /request supervisor review/i });
        await userEvent.click(supervisorBtn);
        expect(await screen.findByRole("dialog")).toBeInTheDocument();
    });
});
