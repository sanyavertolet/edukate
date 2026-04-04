import { useQuery } from "@tanstack/react-query";
import { client } from "@/lib/axios";
import { defaultErrorHandler } from "@/lib/error-handler";
import { queryKeys } from "@/lib/query-keys";
import { ProblemMetadata, Problem, Result } from "./types";

export function useProblemListRequest(page: number, size: number) {
    return useQuery({
        queryKey: queryKeys.problems.list(page, size),
        queryFn: async () => {
            try {
                const response = await client.get(`/api/v1/problems?page=${page}&size=${size}`);
                return response.data as ProblemMetadata[];
            } catch (error) {
                throw defaultErrorHandler(error);
            }
        },
    });
}

export function useProblemCountRequest() {
    return useQuery({
        queryKey: queryKeys.problems.count,
        queryFn: async () => {
            try {
                const response = await client.get<number>("/api/v1/problems/count");
                return response.data;
            } catch (error) {
                throw defaultErrorHandler(error);
            }
        },
    });
}

export function useProblemRequest(id: string | undefined) {
    return useQuery({
        queryKey: queryKeys.problems.detail(id ?? ""),
        queryFn: async () => {
            if (id === undefined) {
                return undefined;
            }
            try {
                const response = await client.get<Problem>(`/api/v1/problems/${id}`);
                return response.data;
            } catch (error) {
                throw defaultErrorHandler(error);
            }
        },
        enabled: id !== undefined,
    });
}

export function useResultRequest(problemId: string) {
    return useQuery({
        queryKey: queryKeys.problems.result(problemId),
        queryFn: async () => {
            try {
                const response = await client.get<Result>(`/api/v1/results/${problemId}`);
                return response.data;
            } catch (error) {
                throw defaultErrorHandler(error);
            }
        },
    });
}

export function useRandomProblemIdQuery() {
    return useQuery({
        queryKey: queryKeys.problems.random,
        queryFn: async () => {
            try {
                const response = await client.get<string>(`/api/v1/problems/random`);
                return response.data;
            } catch (error) {
                throw defaultErrorHandler(error);
            }
        },
        enabled: false,
    });
}
