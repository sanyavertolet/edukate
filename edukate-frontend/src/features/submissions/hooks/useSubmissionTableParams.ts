import { useEffect, useMemo, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { SubmissionStatus } from "@/features/submissions/types";

export const DEFAULT_PAGE_SIZE = 10;

function getSearchParamAsInt(searchParams: URLSearchParams, key: string, defaultValue: number) {
    const value = searchParams.get(key);
    return value ? parseInt(value, 10) : defaultValue;
}

export type StatusFilter = SubmissionStatus | "ALL" | undefined;

export function useSubmissionTableParams() {
    const [searchParams, setSearchParams] = useSearchParams();

    const [page, setPage] = useState(getSearchParamAsInt(searchParams, "page", 0));
    const [rowsPerPage, setRowsPerPage] = useState(getSearchParamAsInt(searchParams, "pageSize", DEFAULT_PAGE_SIZE));
    const [status, setStatus] = useState<StatusFilter>((searchParams.get("status") as StatusFilter) || "ALL");
    const [userName, setUserName] = useState<string>(searchParams.get("userName") || "");
    const [bookSlug, setBookSlug] = useState<string>(searchParams.get("bookSlug") || "");
    const [problemCode, setProblemCode] = useState<string>(searchParams.get("problemCode") || "");

    useEffect(() => {
        setPage(getSearchParamAsInt(searchParams, "page", 0));
        setRowsPerPage(getSearchParamAsInt(searchParams, "pageSize", DEFAULT_PAGE_SIZE));
        setStatus((searchParams.get("status") as StatusFilter) || "ALL");
        setUserName(searchParams.get("userName") || "");
        setBookSlug(searchParams.get("bookSlug") || "");
        setProblemCode(searchParams.get("problemCode") || "");
    }, [searchParams]);

    const updateSearchParams = (
        params: Partial<{
            page: number;
            pageSize: number;
            status: StatusFilter;
            userName: string;
            bookSlug: string;
            problemCode: string;
        }>,
    ) => {
        const next = new URLSearchParams(searchParams);

        if (params.page !== undefined) {
            if (params.page === 0) next.delete("page");
            else next.set("page", String(params.page));
        }
        if (params.pageSize !== undefined) {
            if (params.pageSize === DEFAULT_PAGE_SIZE) next.delete("pageSize");
            else next.set("pageSize", String(params.pageSize));
        }
        if (params.status !== undefined) {
            if (params.status === "ALL") next.delete("status");
            else next.set("status", params.status);
        }
        if (params.userName !== undefined) {
            if (!params.userName) next.delete("userName");
            else next.set("userName", params.userName);
        }
        if (params.bookSlug !== undefined) {
            if (!params.bookSlug) next.delete("bookSlug");
            else next.set("bookSlug", params.bookSlug);
        }
        if (params.problemCode !== undefined) {
            if (!params.problemCode) next.delete("problemCode");
            else next.set("problemCode", params.problemCode);
        }
        setSearchParams(next);
    };

    const handlers = useMemo(
        () => ({
            onChangePage: (_: unknown, newPage: number) => {
                updateSearchParams({ page: newPage });
            },
            onChangeStatus: (newStatus: StatusFilter) => {
                updateSearchParams({ page: 0, status: newStatus });
            },
            onChangeUserName: (newUserName: string) => {
                updateSearchParams({ page: 0, userName: newUserName });
            },
            onChangeBookSlug: (newBookSlug: string) => {
                updateSearchParams({ page: 0, bookSlug: newBookSlug });
            },
            onChangeProblemCode: (newProblemCode: string) => {
                updateSearchParams({ page: 0, problemCode: newProblemCode });
            },
            onChangeRowsPerPage: (value: number) => {
                updateSearchParams({ page: 0, pageSize: value });
            },
        }),
        // eslint-disable-next-line react-hooks/exhaustive-deps
        [searchParams],
    );

    return { page, rowsPerPage, status, userName, bookSlug, problemCode, handlers };
}
