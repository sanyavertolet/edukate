import { useQuery } from "@tanstack/react-query";
import { getProblem, getProblemList, getRandomUnsolvedProblemId, getResultById } from "@/generated/backend";
import type { GetProblemListParams } from "@/generated/backend";
import { client } from "@/lib/axios";
import { queryKeys } from "@/lib/query-keys";
import type { StatusFilter } from "@/features/problems/hooks/useProblemTableParams";

const PROBLEM_STALE_TIME = 5 * 60_000;

export function useProblemListRequest(
    page: number,
    size: number,
    prefix: string,
    status: StatusFilter,
    isHard: boolean | undefined,
    hasPictures: boolean | undefined,
    hasResult: boolean | undefined,
) {
    const effectiveStatus = status === "ALL" ? undefined : status;
    return useQuery({
        queryKey: queryKeys.problems.list(page, size, prefix || undefined, effectiveStatus, isHard, hasPictures, hasResult),
        staleTime: PROBLEM_STALE_TIME,
        queryFn: ({ signal }) =>
            getProblemList(
                {
                    page: String(page),
                    size: String(size),
                    ...(prefix ? { prefix } : {}),
                    ...(effectiveStatus ? { status: effectiveStatus } : {}),
                    ...(isHard !== undefined ? { isHard: String(isHard) } : {}),
                    ...(hasPictures !== undefined ? { hasPictures: String(hasPictures) } : {}),
                    ...(hasResult !== undefined ? { hasResult: String(hasResult) } : {}),
                } as GetProblemListParams,
                signal,
            ),
    });
}

export function useProblemCountRequest(
    prefix: string,
    status: StatusFilter,
    isHard: boolean | undefined,
    hasPictures: boolean | undefined,
    hasResult: boolean | undefined,
) {
    const effectiveStatus = status === "ALL" ? undefined : status;
    return useQuery({
        queryKey: queryKeys.problems.count(prefix || undefined, effectiveStatus, isHard, hasPictures, hasResult),
        staleTime: PROBLEM_STALE_TIME,
        queryFn: ({ signal }) =>
            client<number>({
                url: "/api/v1/problems/count",
                method: "GET",
                params: {
                    ...(prefix ? { prefix } : {}),
                    ...(effectiveStatus ? { status: effectiveStatus } : {}),
                    ...(isHard !== undefined ? { isHard } : {}),
                    ...(hasPictures !== undefined ? { hasPictures } : {}),
                    ...(hasResult !== undefined ? { hasResult } : {}),
                },
                signal,
            }),
    });
}

export function useProblemRequest(id: string | undefined) {
    return useQuery({
        queryKey: queryKeys.problems.detail(id ?? ""),
        staleTime: PROBLEM_STALE_TIME,
        queryFn: ({ signal }) => getProblem(id as string, signal),
        enabled: id !== undefined,
        meta: { silent: true },
    });
}

export function useResultRequest(problemId: string) {
    return useQuery({
        queryKey: queryKeys.problems.result(problemId),
        queryFn: ({ signal }) => getResultById(problemId, signal),
    });
}

export function useRandomProblemIdQuery() {
    return useQuery({
        queryKey: queryKeys.problems.random,
        queryFn: ({ signal }) => getRandomUnsolvedProblemId(signal),
        enabled: false,
    });
}
