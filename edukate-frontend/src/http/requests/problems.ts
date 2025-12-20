import { useQuery } from "@tanstack/react-query";
import { useAuthContext } from "../../components/auth/AuthContextProvider";
import { client } from "../client";
import { ProblemMetadata } from "../../types/problem/ProblemMetadata";
import { defaultErrorHandler } from "../utils";
import { Problem } from "../../types/problem/Problem";
import { Result } from "../../types/problem/Result";

export function useProblemListRequest(page: number, size: number) {
    const { user } = useAuthContext();
    return useQuery({
        queryKey: ['problemList', page, size, user],
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
        queryKey: ['problemCount'],
        queryFn: async () => {
            try {
                const response = await client.get<number>('/api/v1/problems/count');
                return response.data;
            } catch (error) {
                throw defaultErrorHandler(error);
            }
        },
    });
}

export function useProblemRequest(id: string | undefined, shouldRefresh?: boolean) {
    const { user } = useAuthContext();
    return useQuery({
        queryKey: ['problem', id, user, shouldRefresh],
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
    });
}

export function useResultRequest(problemId: string) {
    return useQuery({
        queryKey: ['result', problemId],
        queryFn: async () => {
            try {
                const response = await client.get<Result>(`/api/v1/results/${problemId}`);
                return response.data;
            } catch (error) {
                throw defaultErrorHandler(error);
            }
        }
    })
}

export function useRandomProblemIdQuery() {
    return useQuery({
        queryKey: ['random-problem'],
        queryFn: async () => {
            try {
                const response = await client.get<string>(`/api/v1/problems/random`);
                return response.data;
            } catch (error) {
                throw defaultErrorHandler(error);
            }
        },
        enabled: false
    })
}
