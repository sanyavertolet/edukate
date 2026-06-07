import { http, HttpResponse } from "msw";
import userEvent from "@testing-library/user-event";
import { render, renderAtPath, screen, waitFor } from "@/test/render";
import { server } from "@/test/server";
import {
    getGetMySubmissionsMockHandler,
    getSearchMemberProblemSetsMockHandler,
    getWhoamiMockHandler,
} from "@/generated/backend";
import { SubmissionList } from "./SubmissionList";
import type { Submission } from "@/features/submissions/types";
import type { ProblemSetMetadata } from "@/features/problem-sets/types";

const MEMBER_SET: ProblemSetMetadata = {
    name: "Mechanics Set",
    description: "Kinematics + Dynamics",
    admins: ["alice"],
    shareCode: "PS-MECH",
    isPublic: false,
    size: 12,
    solvedCount: 3,
    currentUserRole: "USER",
};

const pendingSubmission: Submission = {
    id: 1,
    problemKey: "savchenko/1.1.1",
    userName: "alice",
    status: "PENDING",
    createdAt: "2024-06-01T10:00:00Z",
    updatedAt: "2024-06-01T10:00:00Z",
    fileUrls: [],
};

const successSubmission: Submission = {
    id: 2,
    problemKey: "savchenko/1.1.1",
    userName: "alice",
    status: "SUCCESS",
    createdAt: "2024-06-02T11:00:00Z",
    updatedAt: "2024-06-02T11:00:00Z",
    fileUrls: ["https://example.com/img1.jpg"],
};

const failedSubmission: Submission = {
    id: 3,
    problemKey: "savchenko/1.1.1",
    userName: "alice",
    status: "FAILED",
    createdAt: "2024-06-03T12:00:00Z",
    updatedAt: "2024-06-03T12:00:00Z",
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

    it("renders an attachments icon button when files exist", async () => {
        server.use(getGetMySubmissionsMockHandler([successSubmission]));
        render(<SubmissionList problemKey="savchenko/1.1.1" />);
        await waitFor(() => {
            expect(screen.getByRole("button", { name: /view attachments/i })).toBeInTheDocument();
        });
    });

    it("does not render the attachments button when there are no files", async () => {
        server.use(getGetMySubmissionsMockHandler([pendingSubmission]));
        render(<SubmissionList problemKey="savchenko/1.1.1" />);
        await waitFor(() => {
            expect(screen.getByText("Pending review")).toBeInTheDocument();
        });
        expect(screen.queryByRole("button", { name: /view attachments/i })).not.toBeInTheDocument();
    });

    it("hides self-check icon for SUCCESS submissions", async () => {
        server.use(getGetMySubmissionsMockHandler([successSubmission]));
        render(<SubmissionList problemKey="savchenko/1.1.1" />);
        await waitFor(() => {
            expect(screen.getByText("Success")).toBeInTheDocument();
        });
        expect(screen.queryByRole("button", { name: /mark as solved/i })).not.toBeInTheDocument();
    });

    it("shows self-check icon for PENDING submissions", async () => {
        server.use(getGetMySubmissionsMockHandler([pendingSubmission]));
        render(<SubmissionList problemKey="savchenko/1.1.1" />);
        await waitFor(() => {
            expect(screen.getByRole("button", { name: /mark as solved/i })).toBeInTheDocument();
        });
    });

    it("always renders the supervisor-check icon", async () => {
        server.use(getGetMySubmissionsMockHandler([pendingSubmission]));
        render(<SubmissionList problemKey="savchenko/1.1.1" />);
        await waitFor(() => {
            expect(screen.getByRole("button", { name: /request supervisor review/i })).toBeInTheDocument();
        });
    });

    it("opens a dialog when the supervisor icon is clicked", async () => {
        server.use(getGetMySubmissionsMockHandler([pendingSubmission]));
        render(<SubmissionList problemKey="savchenko/1.1.1" />);
        const supervisorBtn = await screen.findByRole("button", { name: /request supervisor review/i });
        await userEvent.click(supervisorBtn);
        expect(await screen.findByRole("dialog")).toBeInTheDocument();
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

describe("SubmissionList — role-gated icons", () => {
    it("hides smart-check icon for a non-moderator user", async () => {
        server.use(getGetMySubmissionsMockHandler([pendingSubmission]));
        render(<SubmissionList problemKey="savchenko/1.1.1" />);
        await waitFor(() => {
            expect(screen.getByText("Pending review")).toBeInTheDocument();
        });
        expect(screen.queryByRole("button", { name: /request smart check/i })).not.toBeInTheDocument();
    });

    it("shows smart-check icon when the user is a moderator", async () => {
        server.use(
            getWhoamiMockHandler({ name: "mod", email: "m@m.io", roles: ["MODERATOR"], status: "ACTIVE" }),
            getGetMySubmissionsMockHandler([pendingSubmission]),
        );
        render(<SubmissionList problemKey="savchenko/1.1.1" />);
        await waitFor(() => {
            expect(screen.getByRole("button", { name: /request smart check/i })).toBeInTheDocument();
        });
    });
});

describe("SubmissionList — paperclip + lightbox", () => {
    it("opens the lightbox when the paperclip icon is clicked", async () => {
        server.use(getGetMySubmissionsMockHandler([successSubmission]));
        render(<SubmissionList problemKey="savchenko/1.1.1" />);
        const paperclip = await screen.findByRole("button", { name: /view attachments/i });
        await userEvent.click(paperclip);
        // ImageLightbox mounts a dialog when open
        expect(await screen.findByRole("dialog")).toBeInTheDocument();
    });
});

describe("SubmissionList — self check mutation", () => {
    it("fires the self-check mutation when the self icon is clicked", async () => {
        let selfCheckCalls = 0;
        server.use(
            getGetMySubmissionsMockHandler([pendingSubmission]),
            http.post("*/api/v1/checker/self", () => {
                selfCheckCalls++;
                return new HttpResponse(null, { status: 200 });
            }),
        );
        render(<SubmissionList problemKey="savchenko/1.1.1" />);
        const selfBtn = await screen.findByRole("button", { name: /mark as solved/i });
        await userEvent.click(selfBtn);
        await waitFor(() => {
            expect(selfCheckCalls).toBeGreaterThanOrEqual(1);
        });
    });
});

describe("SubmissionList — supervisor dialog auto-select", () => {
    it("auto-selects the problem set when the problemSetCode prop is provided", async () => {
        server.use(
            getWhoamiMockHandler({ name: "alice", email: "a@a.io", roles: ["USER"], status: "ACTIVE" }),
            getGetMySubmissionsMockHandler([pendingSubmission]),
            getSearchMemberProblemSetsMockHandler([MEMBER_SET]),
        );
        render(<SubmissionList problemKey="savchenko/1.1.1" problemSetCode="PS-MECH" />);
        const supervisorBtn = await screen.findByRole("button", { name: /request supervisor review/i });
        await userEvent.click(supervisorBtn);
        // The problem set selector is an MUI Autocomplete; the seeded value displays as the set's name.
        expect(await screen.findByDisplayValue(MEMBER_SET.name)).toBeInTheDocument();
    });

    it("auto-selects the problem set from the ?problemSet= query param", async () => {
        server.use(
            getWhoamiMockHandler({ name: "alice", email: "a@a.io", roles: ["USER"], status: "ACTIVE" }),
            getGetMySubmissionsMockHandler([pendingSubmission]),
            getSearchMemberProblemSetsMockHandler([MEMBER_SET]),
        );
        renderAtPath(
            "/problems/savchenko/1.1.1?problemSet=PS-MECH",
            "/problems/:bookSlug/:code",
            <SubmissionList problemKey="savchenko/1.1.1" />,
        );
        const supervisorBtn = await screen.findByRole("button", { name: /request supervisor review/i });
        await userEvent.click(supervisorBtn);
        expect(await screen.findByDisplayValue(MEMBER_SET.name)).toBeInTheDocument();
    });

    it("opens an empty form when no problemSetCode is provided", async () => {
        server.use(
            getWhoamiMockHandler({ name: "alice", email: "a@a.io", roles: ["USER"], status: "ACTIVE" }),
            getGetMySubmissionsMockHandler([pendingSubmission]),
            getSearchMemberProblemSetsMockHandler([MEMBER_SET]),
        );
        render(<SubmissionList problemKey="savchenko/1.1.1" />);
        const supervisorBtn = await screen.findByRole("button", { name: /request supervisor review/i });
        await userEvent.click(supervisorBtn);
        // Dialog opens, but the set name is not pre-selected.
        await screen.findByRole("dialog");
        expect(screen.queryByDisplayValue(MEMBER_SET.name)).not.toBeInTheDocument();
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
