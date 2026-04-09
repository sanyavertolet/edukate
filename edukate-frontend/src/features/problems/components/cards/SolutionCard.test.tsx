import { http, HttpResponse } from "msw";
import { render, screen, waitFor, fireEvent } from "@/test/render";
import { server } from "@/test/server";
import {
    getGetTempFilesMockHandler,
    getUploadTempFileMockHandler,
    getGetResultByIdMockHandler,
    getGetResultByIdResponseMock,
} from "@/generated/backend";
import SolutionCard from "./SolutionCard";
import type { Problem } from "@/features/problems/types";

const problem: Problem = {
    id: "prob-42",
    isHard: false,
    tags: [],
    text: "Solve this problem",
    subtasks: [],
    images: [],
    status: "NOT_SOLVED",
    hasResult: false,
};

/** Simulate selecting files on the hidden <input type="file"> */
function simulateFileSelect(container: HTMLElement, files: File[]) {
    const input = container.querySelector('input[type="file"]') as HTMLInputElement;
    const fileList = Object.assign([...files], {
        item: (i: number) => files[i] ?? null,
    }) as unknown as FileList;
    Object.defineProperty(input, "files", { value: fileList, configurable: true });
    fireEvent.change(input);
}

describe("SolutionCard — static rendering", () => {
    beforeEach(() => {
        server.use(
            getGetTempFilesMockHandler([]),
            getGetResultByIdMockHandler(getGetResultByIdResponseMock({ text: "", images: [] })),
        );
    });

    it("renders the 'Solution' section heading", () => {
        render(<SolutionCard problem={problem} />);
        expect(screen.getByText("Solution")).toBeInTheDocument();
    });

    it("renders the upload instruction text", () => {
        render(<SolutionCard problem={problem} />);
        expect(screen.getByText(/take photos/i)).toBeInTheDocument();
    });

    it("does not show the Submit button when no files have been uploaded", () => {
        render(<SolutionCard problem={problem} />);
        expect(screen.queryByRole("button", { name: /submit/i })).not.toBeInTheDocument();
    });

    it("renders the 'Show the result' accordion", () => {
        render(<SolutionCard problem={problem} />);
        expect(screen.getByText("Show the result")).toBeInTheDocument();
    });
});

describe("SolutionCard — submission pipeline", () => {
    it("Submit button appears after a file is successfully uploaded to temp storage", async () => {
        server.use(
            getGetTempFilesMockHandler([]),
            getUploadTempFileMockHandler("temp-key-abc"),
        );
        const { container } = render(<SolutionCard problem={problem} />);

        simulateFileSelect(container, [new File(["img"], "photo.jpg", { type: "image/jpeg" })]);

        await waitFor(() =>
            expect(screen.getByRole("button", { name: /submit/i })).toBeInTheDocument(),
        );
    });

    it("clicking Submit calls POST /api/v1/submissions with the temp file keys", async () => {
        let submittedBody: unknown;
        server.use(
            getGetTempFilesMockHandler([]),
            getUploadTempFileMockHandler("temp-key-xyz"),
            http.post("*/api/v1/submissions", async ({ request }) => {
                submittedBody = await request.json();
                return HttpResponse.json({ id: "sub-new" });
            }),
        );
        const { container } = render(<SolutionCard problem={problem} />);

        simulateFileSelect(container, [new File(["img"], "solution.jpg", { type: "image/jpeg" })]);

        const submitBtn = await screen.findByRole("button", { name: /submit/i });
        submitBtn.click();

        await waitFor(() => {
            expect(submittedBody).toMatchObject({
                problemId: "prob-42",
                fileNames: ["temp-key-xyz"],
            });
        });
    });

    it("Submit button lists all uploaded files when multiple files are selected", async () => {
        const uploadedKeys: string[] = [];
        server.use(
            getGetTempFilesMockHandler([]),
            // Return a different key for each upload call
            http.post("*/api/v1/files/temp", async () => {
                const key = `key-${uploadedKeys.length + 1}`;
                uploadedKeys.push(key);
                return HttpResponse.text(key);
            }),
        );
        const { container } = render(<SolutionCard problem={problem} />);

        simulateFileSelect(container, [
            new File(["a"], "img1.jpg", { type: "image/jpeg" }),
            new File(["b"], "img2.jpg", { type: "image/jpeg" }),
        ]);

        await waitFor(() => expect(uploadedKeys).toHaveLength(2));
        await waitFor(() =>
            expect(screen.getByRole("button", { name: /submit/i })).toBeInTheDocument(),
        );
    });
});

describe("SolutionCard — pre-existing temp files", () => {
    it("Submit button is visible immediately if temp files already exist on server", async () => {
        server.use(
            getGetTempFilesMockHandler([
                { key: "existing-key", authorName: "alice", lastModified: "2024-01-01T00:00:00Z", size: 100 },
            ]),
        );
        render(<SolutionCard problem={problem} />);
        await waitFor(() =>
            expect(screen.getByRole("button", { name: /submit/i })).toBeInTheDocument(),
        );
    });
});
