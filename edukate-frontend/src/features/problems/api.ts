import { isAxiosError } from "axios";
import { useQuery } from "@tanstack/react-query";
import { getProblem, getProblemList, getRandomUnsolvedProblemKey, getAnswerByProblemKey } from "@/generated/backend";
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
    bookSlug: string | undefined,
) {
    const effectiveStatus = status === "ALL" ? undefined : status;
    return useQuery({
        queryKey: queryKeys.problems.list(
            page,
            size,
            prefix || undefined,
            effectiveStatus,
            isHard,
            hasPictures,
            hasResult,
            bookSlug,
        ),
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
                    ...(bookSlug ? { bookSlug } : {}),
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
    bookSlug: string | undefined,
) {
    const effectiveStatus = status === "ALL" ? undefined : status;
    return useQuery({
        queryKey: queryKeys.problems.count(
            prefix || undefined,
            effectiveStatus,
            isHard,
            hasPictures,
            hasResult,
            bookSlug,
        ),
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
                    ...(bookSlug ? { bookSlug } : {}),
                },
                signal,
            }),
    });
}

export function useProblemRequest(bookSlug: string | undefined, code: string | undefined) {
    return useQuery({
        queryKey: queryKeys.problems.detail(bookSlug && code ? `${bookSlug}/${code}` : ""),
        staleTime: PROBLEM_STALE_TIME,
        queryFn: ({ signal }) => getProblem(bookSlug as string, code as string, signal),
        enabled: bookSlug !== undefined && code !== undefined,
        meta: { silent: true },
    });
}

export function useAnswerRequest(bookSlug: string, code: string) {
    return useQuery({
        queryKey: queryKeys.problems.answer(bookSlug, code),
        queryFn: async ({ signal }) => {
            try {
                return await getAnswerByProblemKey(bookSlug, code, signal);
            } catch (error) {
                if (isAxiosError(error) && error.response?.status === 404) {
                    return null;
                }
                throw error;
            }
        },
    });
}

export function useRandomProblemKeyQuery() {
    return useQuery({
        queryKey: queryKeys.problems.random,
        queryFn: ({ signal }) => getRandomUnsolvedProblemKey(signal),
        enabled: false,
    });
}
