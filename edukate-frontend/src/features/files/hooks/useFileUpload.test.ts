import { renderHook, waitFor, act } from "@testing-library/react";
import type { ChangeEvent } from "react";
import { createWrapper } from "@/test/render";
import { server } from "@/test/server";
import { getDeleteTempFileMockHandler, getGetTempFilesMockHandler, getUploadTempFileMockHandler } from "@/generated/backend";
import { useFileUpload } from "./useFileUpload";

function makeChangeEvent(files: File[]): ChangeEvent<HTMLInputElement> {
    const fileList = Object.assign([...files], {
        item: (i: number) => files[i] ?? null,
    }) as unknown as FileList;
    return {
        target: { files: fileList, value: "" },
    } as ChangeEvent<HTMLInputElement>;
}

describe("useFileUpload", () => {
    it("adds files to the list on handleAddFiles", () => {
        server.use(getGetTempFilesMockHandler([]));
        const { result } = renderHook(() => useFileUpload({ onTempFileUploaded: vi.fn(), onTempFileDeleted: vi.fn() }), {
            wrapper: createWrapper(),
        });
        const file = new File(["content"], "test.txt", { type: "text/plain" });
        act(() => {
            result.current.handleAddFiles(makeChangeEvent([file]));
        });
        expect(result.current.fileMetadataList).toHaveLength(1);
        expect(result.current.fileMetadataList[0].key).toBe("test.txt");
    });

    it("sets errorText when file count exceeds maxFiles", () => {
        server.use(getGetTempFilesMockHandler([]));
        const { result } = renderHook(
            () => useFileUpload({ onTempFileUploaded: vi.fn(), onTempFileDeleted: vi.fn(), maxFiles: 1 }),
            { wrapper: createWrapper() },
        );
        const files = [new File(["a"], "a.txt"), new File(["b"], "b.txt")];
        act(() => {
            result.current.handleAddFiles(makeChangeEvent(files));
        });
        expect(result.current.errorText).toMatch(/no more than 1/i);
        expect(result.current.fileMetadataList).toHaveLength(0);
    });

    it("sets errorText when total file size exceeds maxSize", () => {
        server.use(getGetTempFilesMockHandler([]));
        const { result } = renderHook(
            () => useFileUpload({ onTempFileUploaded: vi.fn(), onTempFileDeleted: vi.fn(), maxSize: 5 }),
            { wrapper: createWrapper() },
        );
        const file = new File(["more than five bytes"], "big.txt");
        act(() => {
            result.current.handleAddFiles(makeChangeEvent([file]));
        });
        expect(result.current.errorText).toMatch(/no more than/i);
        expect(result.current.fileMetadataList).toHaveLength(0);
    });

    it("calls onTempFileUploaded with the server key on upload success", async () => {
        server.use(getGetTempFilesMockHandler([]), getUploadTempFileMockHandler("server-key-123"));
        const onTempFileUploaded = vi.fn();
        const { result } = renderHook(() => useFileUpload({ onTempFileUploaded, onTempFileDeleted: vi.fn() }), {
            wrapper: createWrapper(),
        });
        const file = new File(["content"], "upload.txt", { type: "text/plain" });
        act(() => {
            result.current.handleAddFiles(makeChangeEvent([file]));
        });
        await waitFor(() => {
            expect(onTempFileUploaded).toHaveBeenCalledWith("server-key-123");
        });
    });

    it("calls onTempFileDeleted after removing a success-state file", async () => {
        server.use(
            getGetTempFilesMockHandler([
                { key: "existing-key", authorName: "alice", lastModified: "2024-01-01T00:00:00Z", size: 100 },
            ]),
            getDeleteTempFileMockHandler("existing-key"),
        );
        const onTempFileDeleted = vi.fn();
        const { result } = renderHook(() => useFileUpload({ onTempFileUploaded: vi.fn(), onTempFileDeleted }), {
            wrapper: createWrapper(),
        });
        await waitFor(() => {
            expect(result.current.fileMetadataList).toHaveLength(1);
        });
        act(() => {
            result.current.handleRemoveFile("existing-key");
        });
        await waitFor(() => {
            expect(onTempFileDeleted).toHaveBeenCalledWith("existing-key");
        });
    });

    it("uses the latest onTempFileUploaded callback after a re-render", async () => {
        server.use(getGetTempFilesMockHandler([]), getUploadTempFileMockHandler("key-abc"));
        const initialCb = vi.fn();
        const { result, rerender } = renderHook(
            ({ cb }: { cb: (key: string) => void }) => useFileUpload({ onTempFileUploaded: cb, onTempFileDeleted: vi.fn() }),
            { wrapper: createWrapper(), initialProps: { cb: initialCb } },
        );
        const newCb = vi.fn();
        rerender({ cb: newCb });
        const file = new File(["content"], "ref.txt", { type: "text/plain" });
        act(() => {
            result.current.handleAddFiles(makeChangeEvent([file]));
        });
        await waitFor(() => {
            expect(newCb).toHaveBeenCalledWith("key-abc");
        });
        expect(initialCb).not.toHaveBeenCalled();
    });
});
