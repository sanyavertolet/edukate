import { ChangeEvent, useEffect, useMemo, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { ProblemStatus } from "@/features/problems/types";

export const DEFAULT_PAGE_SIZE = 10;

function getSearchParamAsInt(searchParams: URLSearchParams, key: string, defaultValue: number) {
    const value = searchParams.get(key);
    return value ? parseInt(value, 10) : defaultValue;
}

function getSearchParamAsBoolean(searchParams: URLSearchParams, key: string): boolean | undefined {
    const value = searchParams.get(key);
    return value === null ? undefined : value === "true";
}

export type StatusFilter = ProblemStatus | "ALL" | undefined;

export function useProblemTableParams() {
    const [searchParams, setSearchParams] = useSearchParams();

    const [page, setPage] = useState(getSearchParamAsInt(searchParams, "page", 0));
    const [rowsPerPage, setRowsPerPage] = useState(getSearchParamAsInt(searchParams, "pageSize", DEFAULT_PAGE_SIZE));
    const [status, setStatus] = useState<StatusFilter>((searchParams.get("status") as StatusFilter) || "ALL");
    const [prefix, setPrefix] = useState<string>(searchParams.get("prefix") || "");
    const [isHard, setIsHard] = useState<boolean | undefined>(getSearchParamAsBoolean(searchParams, "isHard"));
    const [hasPictures, setHasPictures] = useState<boolean | undefined>(
        getSearchParamAsBoolean(searchParams, "hasPictures"),
    );
    const [hasResult, setHasResult] = useState<boolean | undefined>(getSearchParamAsBoolean(searchParams, "hasResult"));
    const [bookSlug, setBookSlug] = useState<string | undefined>(searchParams.get("bookSlug") ?? undefined);

    useEffect(() => {
        setPage(getSearchParamAsInt(searchParams, "page", 0));
        setRowsPerPage(getSearchParamAsInt(searchParams, "pageSize", DEFAULT_PAGE_SIZE));
        setStatus((searchParams.get("status") as StatusFilter) || "ALL");
        setPrefix(searchParams.get("prefix") || "");
        setIsHard(getSearchParamAsBoolean(searchParams, "isHard"));
        setHasPictures(getSearchParamAsBoolean(searchParams, "hasPictures"));
        setHasResult(getSearchParamAsBoolean(searchParams, "hasResult"));
        setBookSlug(searchParams.get("bookSlug") ?? undefined);
    }, [searchParams]);

    const updateSearchParams = (
        params: Partial<{
            page: number;
            pageSize: number;
            status: StatusFilter;
            prefix: string;
            isHard: boolean | undefined;
            hasPictures: boolean | undefined;
            hasResult: boolean | undefined;
            bookSlug: string | undefined;
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
        if (params.prefix !== undefined) {
            if (!params.prefix) next.delete("prefix");
            else next.set("prefix", params.prefix);
        }
        if ("isHard" in params) {
            if (params.isHard === undefined) next.delete("isHard");
            else next.set("isHard", String(params.isHard));
        }
        if ("hasPictures" in params) {
            if (params.hasPictures === undefined) next.delete("hasPictures");
            else next.set("hasPictures", String(params.hasPictures));
        }
        if ("hasResult" in params) {
            if (params.hasResult === undefined) next.delete("hasResult");
            else next.set("hasResult", String(params.hasResult));
        }
        if ("bookSlug" in params) {
            if (params.bookSlug === undefined) next.delete("bookSlug");
            else next.set("bookSlug", params.bookSlug);
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
            onChangePrefix: (newPrefix: string) => {
                updateSearchParams({ page: 0, prefix: newPrefix });
            },
            onChangeRowsPerPage: (event: ChangeEvent<HTMLInputElement>) => {
                updateSearchParams({ page: 0, pageSize: parseInt(event.target.value, 10) });
            },
            onChangeIsHard: (value: boolean | undefined) => {
                updateSearchParams({ page: 0, isHard: value });
            },
            onChangeHasPictures: (checked: boolean) => {
                updateSearchParams({ page: 0, hasPictures: checked || undefined });
            },
            onChangeHasResult: (checked: boolean) => {
                updateSearchParams({ page: 0, hasResult: checked || undefined });
            },
            onChangeBookSlug: (slug: string | undefined) => {
                updateSearchParams({ page: 0, bookSlug: slug });
            },
        }),
        [searchParams],
    );

    return { page, rowsPerPage, status, prefix, isHard, hasPictures, hasResult, bookSlug, handlers };
}
