import { render, screen, waitFor } from "@/test/render";
import userEvent from "@testing-library/user-event";
import { server } from "@/test/server";
import {
    getSearchSubmissionsMockHandler,
    getWhoamiMockHandler,
    PageResponseSubmissionDto,
    UserDto,
} from "@/generated/backend";
import SubmissionListComponent from "./SubmissionListComponent";

const mockWhoami: UserDto = {
    name: "testuser",
    roles: ["USER"],
    status: "ACTIVE",
};

function mockPage(overrides: Partial<PageResponseSubmissionDto> = {}): PageResponseSubmissionDto {
    return {
        content: [
            {
                id: 1,
                problemKey: "savchenko/1.1.1",
                userName: "testuser",
                status: "PENDING",
                createdAt: "2024-01-01T00:00:00Z",
                updatedAt: "2024-01-02T00:00:00Z",
                fileUrls: [],
            },
            {
                id: 2,
                problemKey: "savchenko/1.1.2",
                userName: "alice",
                status: "SUCCESS",
                createdAt: "2024-01-01T00:00:00Z",
                updatedAt: "2024-01-03T00:00:00Z",
                fileUrls: [],
            },
        ],
        page: 0,
        size: 10,
        totalElements: 2,
        totalPages: 1,
        ...overrides,
    };
}

beforeEach(() => {
    server.use(getWhoamiMockHandler(mockWhoami));
});

it("renders table with correct column headers", async () => {
    server.use(getSearchSubmissionsMockHandler(mockPage()));

    render(<SubmissionListComponent />);

    await waitFor(() => {
        // "Book" appears both as a column header and toolbar label
        expect(screen.getAllByText("Book").length).toBeGreaterThanOrEqual(2);
        expect(screen.getByText("Problem")).toBeInTheDocument();
        expect(screen.getByText("User")).toBeInTheDocument();
        expect(screen.getByText("Updated")).toBeInTheDocument();
    });
});

it("renders submission rows from API response", async () => {
    server.use(getSearchSubmissionsMockHandler(mockPage()));

    render(<SubmissionListComponent />);

    await waitFor(() => {
        expect(screen.getByText("1.1.1")).toBeInTheDocument();
        expect(screen.getByText("1.1.2")).toBeInTheDocument();
        expect(screen.getByText("testuser")).toBeInTheDocument();
        expect(screen.getByText("alice")).toBeInTheDocument();
    });
});

it("renders filter controls", async () => {
    server.use(getSearchSubmissionsMockHandler(mockPage()));

    render(<SubmissionListComponent />);

    await waitFor(() => {
        expect(screen.getByLabelText("Username")).toBeInTheDocument();
        expect(screen.getByLabelText("Book")).toBeInTheDocument();
        expect(screen.getByLabelText("Problem code")).toBeInTheDocument();
        expect(screen.getByLabelText("Status")).toBeInTheDocument();
    });
});

it("opens submission drawer when a row is clicked", async () => {
    server.use(getSearchSubmissionsMockHandler(mockPage()));

    render(<SubmissionListComponent />);

    await waitFor(() => {
        expect(screen.getByText("1.1.1")).toBeInTheDocument();
    });

    await userEvent.click(screen.getByText("1.1.1"));

    await waitFor(() => {
        expect(screen.getByText("Submission #1")).toBeInTheDocument();
    });
});

it("renders empty state when no submissions", async () => {
    server.use(
        getSearchSubmissionsMockHandler(
            mockPage({
                content: [],
                totalElements: 0,
                totalPages: 0,
            }),
        ),
    );

    render(<SubmissionListComponent />);

    await waitFor(() => {
        expect(
            screen.getByText((_, el) => el !== null && el.tagName === "P" && el.textContent === "0–0 of 0 submissions"),
        ).toBeInTheDocument();
    });
});

it("displays pagination info", async () => {
    server.use(getSearchSubmissionsMockHandler(mockPage()));

    render(<SubmissionListComponent />);

    await waitFor(() => {
        expect(
            screen.getByText(
                (_, el) => el !== null && el.tagName === "P" && (el.textContent?.includes("of 2 submissions") ?? false),
            ),
        ).toBeInTheDocument();
    });
});

it("renders skeleton loading state", async () => {
    server.use(
        getWhoamiMockHandler(mockWhoami),
        getSearchSubmissionsMockHandler(async () => {
            return await new Promise<PageResponseSubmissionDto>(() => {});
        }),
    );

    render(<SubmissionListComponent />);

    await waitFor(() => {
        const skeletons = document.querySelectorAll(".MuiSkeleton-root");
        expect(skeletons.length).toBeGreaterThan(0);
    });
});
