import { useQuery } from "@tanstack/react-query";
import { count, getProblem, getProblemList, getRandomUnsolvedProblemId, getResultById } from "@/generated/backend";
import { queryKeys } from "@/lib/query-keys";

const PROBLEM_STALE_TIME = 5 * 60_000;

export function useProblemListRequest(page: number, size: number) {
    return useQuery({
        queryKey: queryKeys.problems.list(page, size),
        staleTime: PROBLEM_STALE_TIME,
        queryFn: ({ signal }) => getProblemList({ page: String(page), size: String(size) }, signal),
    });
}

export function useProblemCountRequest() {
    return useQuery({
        queryKey: queryKeys.problems.count,
        staleTime: PROBLEM_STALE_TIME,
        queryFn: ({ signal }) => count(signal),
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
