import { ChangeEvent, useEffect, useMemo, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { ProblemStatus } from '../types/problem/ProblemMetadata';

export const DEFAULT_PAGE_SIZE = 10;

function getSearchParamAsInt(searchParams: URLSearchParams, key: string, defaultValue: number) {
    return searchParams.get(key) ? parseInt(searchParams.get(key)!, 10) : defaultValue;
}

export type StatusFilter = ProblemStatus | "ALL" | undefined;

export function useProblemTableParams() {
    const [searchParams, setSearchParams] = useSearchParams();

    const [page, setPage] = useState(getSearchParamAsInt(searchParams, "page", 0));
    const [rowsPerPage, setRowsPerPage] = useState(getSearchParamAsInt(searchParams, "pageSize", DEFAULT_PAGE_SIZE));
    const [status, setStatus] = useState<StatusFilter>(
        (searchParams.get("status") as StatusFilter) || "ALL"
    );
    const [prefix, setPrefix] = useState<string>(searchParams.get("prefix") || "");

    useEffect(() => {
        setPage(getSearchParamAsInt(searchParams, "page", 0));
        setRowsPerPage(getSearchParamAsInt(searchParams, "pageSize", DEFAULT_PAGE_SIZE));
        setStatus((searchParams.get("status") as StatusFilter) || "ALL");
        setPrefix(searchParams.get('prefix') || "");
    }, [searchParams]);

    const updateSearchParams = (
        params: Partial<{ page: number; pageSize: number; status: StatusFilter; prefix: string }>
    ) => {
        const next = new URLSearchParams(searchParams);

        if (params.page !== undefined) {
            if (params.page === 0) next.delete("page"); else next.set("page", String(params.page));
        }
        if (params.pageSize !== undefined) {
            if (params.pageSize === DEFAULT_PAGE_SIZE) next.delete("pageSize"); else next.set("pageSize", String(params.pageSize));
        }
        if (params.status !== undefined) {
            if (!params.status || params.status === 'ALL') next.delete("status"); else next.set("status", params.status);
        }
        if (params.prefix !== undefined) {
            if (!params.prefix) next.delete("prefix"); else next.set("prefix", params.prefix);
        }
        setSearchParams(next);
    };

    const handlers = useMemo(() => ({
        onChangePage: (_: unknown, newPage: number) => updateSearchParams({ page: newPage }),
        onChangeStatus: (newStatus: StatusFilter) => updateSearchParams({ page: 0, status: newStatus }),
        onChangePrefix: (newPrefix: string) => updateSearchParams({ page: 0, prefix: newPrefix }),
        onChangeRowsPerPage: (event: ChangeEvent<HTMLInputElement>) =>
            updateSearchParams({ page: 0, pageSize: parseInt(event.target.value, 10)}),
    }), [searchParams]);

    return { page, rowsPerPage, status, prefix, handlers };
}
