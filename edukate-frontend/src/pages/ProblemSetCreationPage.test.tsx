import { useLocation } from "react-router-dom";
import userEvent from "@testing-library/user-event";
import { http, HttpResponse } from "msw";
import { render, screen, waitFor } from "@/test/render";
import { server } from "@/test/server";
import { getCreateProblemSetResponseMock } from "@/generated/backend";
import ProblemSetCreationPage from "./ProblemSetCreationPage";

const LocationSpy = () => <span data-testid="pathname">{useLocation().pathname}</span>;

describe("ProblemSetCreationPage — static rendering", () => {
    it("renders 'Create Problem Set' heading", () => {
        render(<ProblemSetCreationPage />);
        expect(screen.getByRole("heading", { name: /create problem set/i })).toBeInTheDocument();
    });

    it("renders Title, Description, and Problems fields", () => {
        render(<ProblemSetCreationPage />);
        expect(screen.getByLabelText(/title/i)).toBeInTheDocument();
        expect(screen.getByLabelText(/description/i)).toBeInTheDocument();
        expect(screen.getByLabelText(/problems/i)).toBeInTheDocument();
    });

    it("renders 'Create Problem Set' button", () => {
        render(<ProblemSetCreationPage />);
        expect(screen.getByRole("button", { name: /create problem set/i })).toBeInTheDocument();
    });
});

describe("ProblemSetCreationPage — field interactions", () => {
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
});

describe("ProblemSetCreationPage — Create Problem Set button", () => {
    it("clicking the button without filling required fields does not crash", async () => {
        render(<ProblemSetCreationPage />);
        // The mutation throws internally ("Invalid problem set request") — no visible error yet (todo)
        await userEvent.click(screen.getByRole("button", { name: /create problem set/i }));
        expect(screen.getByRole("button", { name: /create problem set/i })).toBeInTheDocument();
    });
});

describe("ProblemSetCreationPage — full creation pipeline", () => {
    // These tests wait out the 1000ms debounce in PrefixOptionInputForm — allow extra time
    const DEBOUNCE_TIMEOUT = 3000;

    it("fills all fields, selects a problem, creates problem set and navigates to it", async () => {
        const problemSetResponse = getCreateProblemSetResponseMock({ shareCode: "ps-xyz" });
        server.use(
            http.get("*/api/v1/problems/by-prefix", () => HttpResponse.json(["prob-001", "prob-002"])),
            http.post("*/api/v1/problem-sets", () => HttpResponse.json(problemSetResponse)),
        );

        render(
            <>
                <ProblemSetCreationPage />
                <LocationSpy />
            </>,
        );

        await userEvent.type(screen.getByLabelText(/title/i), "My Problem Set");
        await userEvent.type(screen.getByLabelText(/description/i), "Test description");

        // Type in the autocomplete — triggers debounced search after 1000ms
        await userEvent.type(screen.getByRole("combobox"), "prob");

        // findByRole waits up to DEBOUNCE_TIMEOUT for the option to appear post-debounce
        const option = await screen.findByRole("option", { name: "prob-001" }, { timeout: DEBOUNCE_TIMEOUT });
        await userEvent.click(option);

        expect(screen.getByText("prob-001")).toBeInTheDocument();

        await userEvent.click(screen.getByRole("button", { name: /create problem set/i }));

        await waitFor(
            () => {
                expect(screen.getByTestId("pathname")).toHaveTextContent("/problem-sets/ps-xyz");
            },
            { timeout: DEBOUNCE_TIMEOUT },
        );
    }, 10000);

    it("shows the selected problem as a chip in the autocomplete", async () => {
        server.use(http.get("*/api/v1/problems/by-prefix", () => HttpResponse.json(["algebra-01"])));

        render(<ProblemSetCreationPage />);

        await userEvent.type(screen.getByRole("combobox"), "alg");

        const option = await screen.findByRole("option", { name: "algebra-01" }, { timeout: DEBOUNCE_TIMEOUT });
        await userEvent.click(option);

        expect(screen.getByText("algebra-01")).toBeInTheDocument();
    }, 10000);
});
