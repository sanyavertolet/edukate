import { renderHook, act, waitFor } from "@testing-library/react";
import { createWrapper } from "@/test/render";
import { useSubmissionTableParams, DEFAULT_PAGE_SIZE } from "./useSubmissionTableParams";

describe("useSubmissionTableParams", () => {
    it("returns default values when no search params are set", () => {
        const { result } = renderHook(() => useSubmissionTableParams(), { wrapper: createWrapper() });

        expect(result.current.page).toBe(0);
        expect(result.current.rowsPerPage).toBe(DEFAULT_PAGE_SIZE);
        expect(result.current.status).toBe("ALL");
        expect(result.current.userName).toBe("");
        expect(result.current.bookSlug).toBe("");
        expect(result.current.problemCode).toBe("");
    });

    it("onChangeStatus updates status and resets page", async () => {
        const { result } = renderHook(() => useSubmissionTableParams(), { wrapper: createWrapper() });

        act(() => {
            result.current.handlers.onChangeStatus("SUCCESS");
        });

        await waitFor(() => {
            expect(result.current.status).toBe("SUCCESS");
            expect(result.current.page).toBe(0);
        });
    });

    it("onChangePage updates page", async () => {
        const { result } = renderHook(() => useSubmissionTableParams(), { wrapper: createWrapper() });

        act(() => {
            result.current.handlers.onChangePage(null, 3);
        });

        await waitFor(() => {
            expect(result.current.page).toBe(3);
        });
    });

    it("onChangeRowsPerPage resets page to 0", async () => {
        const { result } = renderHook(() => useSubmissionTableParams(), { wrapper: createWrapper() });

        act(() => {
            result.current.handlers.onChangePage(null, 2);
        });

        await waitFor(() => {
            expect(result.current.page).toBe(2);
        });

        act(() => {
            result.current.handlers.onChangeRowsPerPage(25);
        });

        await waitFor(() => {
            expect(result.current.rowsPerPage).toBe(25);
            expect(result.current.page).toBe(0);
        });
    });

    it("onChangeUserName updates userName and resets page", async () => {
        const { result } = renderHook(() => useSubmissionTableParams(), { wrapper: createWrapper() });

        act(() => {
            result.current.handlers.onChangeUserName("alice");
        });

        await waitFor(() => {
            expect(result.current.userName).toBe("alice");
            expect(result.current.page).toBe(0);
        });
    });

    it("onChangeBookSlug updates bookSlug, resets page, and clears problemCode", async () => {
        const { result } = renderHook(() => useSubmissionTableParams(), { wrapper: createWrapper() });

        act(() => {
            result.current.handlers.onChangeProblemCode("1.3");
        });
        await waitFor(() => {
            expect(result.current.problemCode).toBe("1.3");
        });

        act(() => {
            result.current.handlers.onChangeBookSlug("savchenko");
        });

        await waitFor(() => {
            expect(result.current.bookSlug).toBe("savchenko");
            expect(result.current.problemCode).toBe("");
            expect(result.current.page).toBe(0);
        });
    });

    it("onChangeProblemKey splits the key into bookSlug and problemCode", async () => {
        const { result } = renderHook(() => useSubmissionTableParams(), { wrapper: createWrapper() });

        act(() => {
            result.current.handlers.onChangeProblemKey("savchenko/1.3.5");
        });

        await waitFor(() => {
            expect(result.current.bookSlug).toBe("savchenko");
            expect(result.current.problemCode).toBe("1.3.5");
            expect(result.current.page).toBe(0);
        });
    });

    it("onChangeProblemKey handles nested codes (bookSlug/chapter/code)", async () => {
        const { result } = renderHook(() => useSubmissionTableParams(), { wrapper: createWrapper() });

        act(() => {
            result.current.handlers.onChangeProblemKey("irodov/1/1.1");
        });

        await waitFor(() => {
            expect(result.current.bookSlug).toBe("irodov");
            expect(result.current.problemCode).toBe("1/1.1");
        });
    });

    it("onChangeProblemCode updates problemCode and resets page", async () => {
        const { result } = renderHook(() => useSubmissionTableParams(), { wrapper: createWrapper() });

        act(() => {
            result.current.handlers.onChangeProblemCode("1.3");
        });

        await waitFor(() => {
            expect(result.current.problemCode).toBe("1.3");
            expect(result.current.page).toBe(0);
        });
    });
});
