import { render, screen } from "@/test/render";
import { server } from "@/test/server";
import { getGetUserRolesMockHandler, getGetInvitedUsersMockHandler } from "@/generated/backend";
import { ProblemSetSettingsTab } from "./ProblemSetSettingsTab";
import { ProblemSet } from "@/features/problem-sets/types";
import { ProblemMetadata } from "@/features/problems/types";

function makeProblem(status: ProblemMetadata["status"], code: string = "1.1"): ProblemMetadata {
    return { key: `savchenko/${code}`, code, bookSlug: "savchenko", isHard: false, tags: [], status };
}

const baseProblemSet: ProblemSet = {
    name: "Mechanics",
    description: "Classical mechanics",
    admins: ["alice"],
    isPublic: true,
    shareCode: "MECH01",
    problems: [makeProblem("SOLVED", "1.1"), makeProblem("NOT_SOLVED", "1.2"), makeProblem("NOT_SOLVED", "1.3")],
};

describe("ProblemSetSettingsTab", () => {
    beforeEach(() => {
        server.use(getGetUserRolesMockHandler([]), getGetInvitedUsersMockHandler([]));
    });

    it("shows problem set name in the summary header", () => {
        render(<ProblemSetSettingsTab problemSet={baseProblemSet} />);
        expect(screen.getByText("Mechanics")).toBeInTheDocument();
    });

    it("shows total problem count", () => {
        render(<ProblemSetSettingsTab problemSet={baseProblemSet} />);
        expect(screen.getByText("3 problems")).toBeInTheDocument();
    });

    it("shows solved/total progress", () => {
        render(<ProblemSetSettingsTab problemSet={baseProblemSet} />);
        expect(screen.getByText("1/3 solved")).toBeInTheDocument();
    });

    it("shows share code chip", () => {
        render(<ProblemSetSettingsTab problemSet={baseProblemSet} />);
        expect(screen.getByText("MECH01")).toBeInTheDocument();
    });

    it("shows Public label when problem set is public", () => {
        render(<ProblemSetSettingsTab problemSet={baseProblemSet} />);
        expect(screen.getByText("Public")).toBeInTheDocument();
        expect(screen.getByText("Anyone can find and join this problem set.")).toBeInTheDocument();
    });

    it("shows Private label when problem set is private", () => {
        render(<ProblemSetSettingsTab problemSet={{ ...baseProblemSet, isPublic: false }} />);
        expect(screen.getByText("Private")).toBeInTheDocument();
        expect(screen.getByText("Only invited users can access this problem set.")).toBeInTheDocument();
    });

    it("renders visibility switch checked when public", () => {
        render(<ProblemSetSettingsTab problemSet={baseProblemSet} />);
        const switchEl = screen.getByRole("checkbox");
        expect(switchEl).toBeChecked();
    });

    it("renders visibility switch unchecked when private", () => {
        render(<ProblemSetSettingsTab problemSet={{ ...baseProblemSet, isPublic: false }} />);
        const switchEl = screen.getByRole("checkbox");
        expect(switchEl).not.toBeChecked();
    });
});
