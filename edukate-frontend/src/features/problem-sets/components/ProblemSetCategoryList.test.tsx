import { render, screen, waitFor } from "@/test/render";
import { server } from "@/test/server";
import { getGetPublicProblemSetsMockHandler } from "@/generated/backend";
import { ProblemSetCategoryList } from "./ProblemSetCategoryList";
import { ProblemSetMetadata } from "@/features/problem-sets/types";
import { HttpResponse } from "msw";

const sampleSets: ProblemSetMetadata[] = [
    {
        name: "Mechanics",
        description: "Classical mechanics",
        admins: ["alice"],
        shareCode: "mech01",
        isPublic: true,
        size: 5,
        solvedCount: 2,
    },
    {
        name: "Optics",
        description: undefined,
        admins: ["bob", "eve"],
        shareCode: "opt02",
        isPublic: false,
        size: 3,
        solvedCount: 0,
    },
];

describe("ProblemSetCategoryList", () => {
    it("shows skeleton items while loading", () => {
        server.use(
            getGetPublicProblemSetsMockHandler(async () => {
                await new Promise(() => {});
                return HttpResponse.json([]);
            }),
        );
        render(<ProblemSetCategoryList tab="public" />);
        const skeletons = document.querySelectorAll(".MuiSkeleton-root");
        expect(skeletons.length).toBeGreaterThan(0);
    });

    it("shows empty state when data is empty", async () => {
        server.use(getGetPublicProblemSetsMockHandler([]));
        render(<ProblemSetCategoryList tab="public" />);
        expect(await screen.findByText("No public problem sets available yet.")).toBeInTheDocument();
    });

    it("renders list items when data is present", async () => {
        server.use(getGetPublicProblemSetsMockHandler(sampleSets));
        render(<ProblemSetCategoryList tab="public" />);
        expect(await screen.findByText("Mechanics")).toBeInTheDocument();
        expect(screen.getByText("Optics")).toBeInTheDocument();
    });

    it("passes onTabSwitch to empty state", async () => {
        const onTabSwitch = vi.fn();
        server.use(getGetPublicProblemSetsMockHandler([]));
        render(<ProblemSetCategoryList tab="public" onTabSwitch={onTabSwitch} />);
        await waitFor(() => {
            expect(screen.getByText("No public problem sets available yet.")).toBeInTheDocument();
        });
    });
});
