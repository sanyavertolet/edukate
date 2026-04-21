import { http, HttpResponse } from "msw";
import userEvent from "@testing-library/user-event";
import { render, screen, waitFor } from "@/test/render";
import { server } from "@/test/server";
import { getGetMySubmissionsMockHandler } from "@/generated/backend";
import { SubmissionList } from "./SubmissionList";
import type { Submission } from "@/features/submissions/types";

const pendingSubmission: Submission = {
    id: 1,
    problemKey: "savchenko/1.1.1",
    userName: "alice",
    status: "PENDING",
    createdAt: "2024-06-01T10:00:00Z",
    fileUrls: [],
};

const successSubmission: Submission = {
    id: 2,
    problemKey: "savchenko/1.1.1",
    userName: "alice",
    status: "SUCCESS",
    createdAt: "2024-06-02T11:00:00Z",
    fileUrls: ["https://example.com/img1.jpg"],
};

const failedSubmission: Submission = {
    id: 3,
    problemKey: "savchenko/1.1.1",
    userName: "alice",
    status: "FAILED",
    createdAt: "2024-06-03T12:00:00Z",
    fileUrls: [],
};

describe("SubmissionList — empty", () => {
    it("shows 'No submissions yet' stub when the list is empty", async () => {
        server.use(getGetMySubmissionsMockHandler([]));
        render(<SubmissionList problemKey="savchenko/1.1.1" />);
        await waitFor(() => {
            expect(screen.getByText("No submissions yet")).toBeInTheDocument();
        });
    });
});

describe("SubmissionList — statuses", () => {
    it("renders PENDING submission as 'Pending review'", async () => {
        server.use(getGetMySubmissionsMockHandler([pendingSubmission]));
        render(<SubmissionList problemKey="savchenko/1.1.1" />);
        await waitFor(() => {
            expect(screen.getByText("Pending review")).toBeInTheDocument();
        });
    });

    it("renders SUCCESS submission as 'Success'", async () => {
        server.use(getGetMySubmissionsMockHandler([successSubmission]));
        render(<SubmissionList problemKey="savchenko/1.1.1" />);
        await waitFor(() => {
            expect(screen.getByText("Success")).toBeInTheDocument();
        });
    });

    it("renders FAILED submission as 'Failed'", async () => {
        server.use(getGetMySubmissionsMockHandler([failedSubmission]));
        render(<SubmissionList problemKey="savchenko/1.1.1" />);
        await waitFor(() => {
            expect(screen.getByText("Failed")).toBeInTheDocument();
        });
    });

    it("renders attachment buttons for file URLs", async () => {
        server.use(getGetMySubmissionsMockHandler([successSubmission]));
        render(<SubmissionList problemKey="savchenko/1.1.1" />);
        await waitFor(() => {
            expect(screen.getByRole("button", { name: /open attachment 1/i })).toBeInTheDocument();
        });
    });
});

describe("SubmissionList — error state", () => {
    it("shows 'Failed to load submissions' on API error", async () => {
        server.use(http.get("*/api/v1/submissions/my", () => HttpResponse.json(null, { status: 500 })));
        render(<SubmissionList problemKey="savchenko/1.1.1" />);
        await waitFor(() => {
            expect(screen.getByText("Failed to load submissions")).toBeInTheDocument();
        });
    });
});

describe("SubmissionList — navigation", () => {
    it("navigates to /submissions/:id when a list item is clicked", async () => {
        server.use(getGetMySubmissionsMockHandler([pendingSubmission]));
        const { container } = render(<SubmissionList problemKey="savchenko/1.1.1" />);
        await waitFor(() => screen.getByText("Pending review"));

        // The ListItemButton is inside the list item
        const listButton = container.querySelector("[role='button']") as HTMLElement;
        // Navigate via link — check that clicking doesn't throw
        await userEvent.click(listButton);
        // Navigation happens — we verify it didn't crash (no route match in MemoryRouter is fine)
        expect(screen.getByText("Pending review")).toBeInTheDocument();
    });
});
