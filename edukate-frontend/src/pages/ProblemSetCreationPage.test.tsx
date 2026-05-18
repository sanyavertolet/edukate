import { useLocation } from "react-router-dom";
import userEvent from "@testing-library/user-event";
import { http, HttpResponse } from "msw";
import { render, screen, waitFor } from "@/test/render";
import { server } from "@/test/server";
import { getCreateProblemSetResponseMock } from "@/generated/backend";
import { ProblemMetadata } from "@/generated/backend";
import ProblemSetCreationPage from "./ProblemSetCreationPage";

const LocationSpy = () => <span data-testid="pathname">{useLocation().pathname}</span>;

const PROBLEM_MOCK: ProblemMetadata = {
    key: "savchenko/1.1.1",
    code: "1.1.1",
    bookSlug: "savchenko",
    isHard: false,
    tags: [],
    status: "NOT_SOLVED",
    createdAt: "2024-01-01T00:00:00Z",
    language: "EN",
};

const problemsHandler = http.get("*/api/v1/problems", () => HttpResponse.json([PROBLEM_MOCK]));
const emptyProblemsHandler = http.get("*/api/v1/problems", () => HttpResponse.json([]));

describe("ProblemSetCreationPage — static rendering (step 0)", () => {
    it("renders 'Create Problem Set' heading", () => {
        render(<ProblemSetCreationPage />);
        expect(screen.getByRole("heading", { name: /create problem set/i })).toBeInTheDocument();
    });

    it("renders Title and Description fields on initial load", () => {
        render(<ProblemSetCreationPage />);
        expect(screen.getByLabelText(/title/i)).toBeInTheDocument();
        expect(screen.getByLabelText(/description/i)).toBeInTheDocument();
    });

    it("renders the stepper with three steps", () => {
        render(<ProblemSetCreationPage />);
        expect(screen.getByText(/details/i)).toBeInTheDocument();
        expect(screen.getByText(/problems/i)).toBeInTheDocument();
        expect(screen.getByText(/invite users/i)).toBeInTheDocument();
    });

    it("shows Next button but not Create on step 0", () => {
        render(<ProblemSetCreationPage />);
        expect(screen.getByRole("button", { name: "Next" })).toBeInTheDocument();
        expect(screen.queryByRole("button", { name: /create problem set/i })).not.toBeInTheDocument();
    });

    it("Next button is always enabled on step 0 (validation shown via step label error)", () => {
        render(<ProblemSetCreationPage />);
        expect(screen.getByRole("button", { name: "Next" })).not.toBeDisabled();
    });
});

describe("ProblemSetCreationPage — step 0 interactions", () => {
    it("Title field accepts input", async () => {
        render(<ProblemSetCreationPage />);
        const titleInput = screen.getByLabelText(/title/i);
        await userEvent.type(titleInput, "My Problem Set");
        expect(titleInput).toHaveValue("My Problem Set");
    });

    it("Description field accepts input", async () => {
        render(<ProblemSetCreationPage />);
        const descInput = screen.getByLabelText(/description/i);
        await userEvent.type(descInput, "A great collection of problems");
        expect(descInput).toHaveValue("A great collection of problems");
    });

    it("Next button enables after filling title and description", async () => {
        render(<ProblemSetCreationPage />);
        await userEvent.type(screen.getByLabelText(/title/i), "My Set");
        await userEvent.type(screen.getByLabelText(/description/i), "Desc");
        expect(screen.getByRole("button", { name: "Next" })).not.toBeDisabled();
    });
});

describe("ProblemSetCreationPage — step navigation", () => {
    it("advances to Problems step and shows Next and Back buttons (no Create yet)", async () => {
        server.use(emptyProblemsHandler);
        render(<ProblemSetCreationPage />);
        await userEvent.type(screen.getByLabelText(/title/i), "My Set");
        await userEvent.type(screen.getByLabelText(/description/i), "Desc");
        await userEvent.click(screen.getByRole("button", { name: "Next" }));

        expect(screen.queryByRole("button", { name: /create problem set/i })).not.toBeInTheDocument();
        expect(screen.getByRole("button", { name: "Next" })).toBeInTheDocument();
        expect(screen.getByRole("button", { name: /back/i })).toBeInTheDocument();
    });

    it("Create button only appears on the final (invite users) step", async () => {
        server.use(emptyProblemsHandler);
        render(<ProblemSetCreationPage />);

        expect(screen.queryByRole("button", { name: /create problem set/i })).not.toBeInTheDocument();

        await userEvent.click(screen.getByRole("button", { name: "Next" }));
        expect(screen.queryByRole("button", { name: /create problem set/i })).not.toBeInTheDocument();

        await userEvent.click(screen.getByRole("button", { name: "Next" }));
        expect(screen.getByRole("button", { name: /create problem set/i })).toBeInTheDocument();
    });

    it("Create button is disabled on final step when no problems were selected", async () => {
        server.use(emptyProblemsHandler);
        render(<ProblemSetCreationPage />);
        await userEvent.type(screen.getByLabelText(/title/i), "My Set");
        await userEvent.type(screen.getByLabelText(/description/i), "Desc");
        await userEvent.click(screen.getByRole("button", { name: "Next" }));
        await userEvent.click(screen.getByRole("button", { name: "Next" }));

        expect(screen.getByRole("button", { name: /create problem set/i })).toBeDisabled();
    });

    it("Back navigates from Problems step to Details", async () => {
        server.use(emptyProblemsHandler);
        render(<ProblemSetCreationPage />);
        await userEvent.type(screen.getByLabelText(/title/i), "My Set");
        await userEvent.type(screen.getByLabelText(/description/i), "Desc");
        await userEvent.click(screen.getByRole("button", { name: "Next" }));
        await userEvent.click(screen.getByRole("button", { name: /back/i }));

        expect(screen.getByLabelText(/title/i)).toBeInTheDocument();
        expect(screen.queryByRole("button", { name: /create problem set/i })).not.toBeInTheDocument();
    });
});

describe("ProblemSetCreationPage — full creation pipeline", () => {
    it("fills details, selects a problem, creates problem set and navigates to it", async () => {
        const problemSetResponse = getCreateProblemSetResponseMock({ shareCode: "ps-xyz" });
        server.use(
            problemsHandler,
            http.post("*/api/v1/problem-sets", () => HttpResponse.json(problemSetResponse)),
        );

        render(
            <>
                <ProblemSetCreationPage />
                <LocationSpy />
            </>,
        );

        // Step 0: fill details
        await userEvent.type(screen.getByLabelText(/title/i), "My Problem Set");
        await userEvent.type(screen.getByLabelText(/description/i), "Test description");
        await userEvent.click(screen.getByRole("button", { name: "Next" }));

        // Step 1: select problem from list
        await userEvent.click(await screen.findByText("savchenko/1.1.1"));

        // Advance to final step (invite users)
        await userEvent.click(screen.getByRole("button", { name: "Next" }));

        expect(screen.getByRole("button", { name: /create problem set/i })).not.toBeDisabled();
        await userEvent.click(screen.getByRole("button", { name: /create problem set/i }));

        await waitFor(
            () => {
                expect(screen.getByTestId("pathname")).toHaveTextContent("/problem-sets/ps-xyz");
            },
            { timeout: 3000 },
        );
    }, 10000);
});
