import { render, screen } from "@/test/render";
import { ProblemSetDescriptionTab } from "./ProblemSetDescriptionTab";
import { ProblemSet } from "@/features/problem-sets/types";
import { ProblemMetadata } from "@/features/problems/types";

function makeProblem(status: ProblemMetadata["status"], code: string = "1.1"): ProblemMetadata {
    return { key: `savchenko/${code}`, code, bookSlug: "savchenko", isHard: false, tags: [], status };
}

const baseProblemSet: ProblemSet = {
    name: "Mechanics",
    description: "Classical mechanics problems",
    admins: ["alice", "bob"],
    isPublic: true,
    shareCode: "MECH01",
    problems: [
        makeProblem("SOLVED", "1.1"),
        makeProblem("FAILED", "1.2"),
        makeProblem("SOLVING", "1.3"),
        makeProblem("NOT_SOLVED", "1.4"),
    ],
};

describe("ProblemSetDescriptionTab", () => {
    it("shows description text", () => {
        render(<ProblemSetDescriptionTab problemSet={baseProblemSet} />);
        expect(screen.getByText("Classical mechanics problems")).toBeInTheDocument();
    });

    it("hides description when not provided", () => {
        render(<ProblemSetDescriptionTab problemSet={{ ...baseProblemSet, description: undefined }} />);
        expect(screen.queryByText("Classical mechanics problems")).not.toBeInTheDocument();
    });

    it("computes correct progress percentage", () => {
        render(<ProblemSetDescriptionTab problemSet={baseProblemSet} />);
        expect(screen.getByText("1 of 4 problems solved (25%)")).toBeInTheDocument();
    });

    it("shows 100% when all problems are solved", () => {
        const allSolved: ProblemSet = {
            ...baseProblemSet,
            problems: [makeProblem("SOLVED", "1.1"), makeProblem("SOLVED", "1.2")],
        };
        render(<ProblemSetDescriptionTab problemSet={allSolved} />);
        expect(screen.getByText("2 of 2 problems solved (100%)")).toBeInTheDocument();
    });

    it("shows 0% when no problems are solved", () => {
        const noneSolved: ProblemSet = {
            ...baseProblemSet,
            problems: [makeProblem("NOT_SOLVED", "1.1"), makeProblem("FAILED", "1.2")],
        };
        render(<ProblemSetDescriptionTab problemSet={noneSolved} />);
        expect(screen.getByText("0 of 2 problems solved (0%)")).toBeInTheDocument();
    });

    it("renders correct status breakdown counts", () => {
        render(<ProblemSetDescriptionTab problemSet={baseProblemSet} />);
        expect(screen.getByText("1 Solved")).toBeInTheDocument();
        expect(screen.getByText("1 Failed")).toBeInTheDocument();
        expect(screen.getByText("1 Pending")).toBeInTheDocument();
        expect(screen.getByText("1 Todo")).toBeInTheDocument();
    });

    it("shows share code", () => {
        render(<ProblemSetDescriptionTab problemSet={baseProblemSet} />);
        expect(screen.getByText("MECH01")).toBeInTheDocument();
    });

    it("shows Public for public problem sets", () => {
        render(<ProblemSetDescriptionTab problemSet={baseProblemSet} />);
        expect(screen.getByText("Public")).toBeInTheDocument();
    });

    it("shows Private for private problem sets", () => {
        render(<ProblemSetDescriptionTab problemSet={{ ...baseProblemSet, isPublic: false }} />);
        expect(screen.getByText("Private")).toBeInTheDocument();
    });

    it("renders admin avatars", () => {
        render(<ProblemSetDescriptionTab problemSet={baseProblemSet} />);
        expect(screen.getByText("al")).toBeInTheDocument();
        expect(screen.getByText("bo")).toBeInTheDocument();
    });
});
