import { useMutation, useQuery } from "@tanstack/react-query";
import { ProblemMetadata } from "../types/ProblemMetadata";
import { client } from "./client";
import axios from "axios";
import { Problem } from "../types/Problem";
import { Submission } from "../types/Submission";
import { useAuthContext } from "../components/auth/AuthContextProvider";
import { Result } from "../types/Result";
import { fullStatus } from "../utils/utils";
import { Bundle } from "../types/Bundle";
import { BundleMetadata } from "../types/BundleMetadata";
import { CreateBundleRequest } from "../types/CreateBundleRequest";

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

export function useSubmitMutation(problemId: string) {
    const { user } = useAuthContext();
    return useMutation({
        mutationKey: ['submit', problemId, user],
        mutationFn: async () => {
            try {
                const response = await client.post<Submission>(`/api/v1/submissions/${problemId}`);
                return response.data;
            } catch (error) {
                throw defaultErrorHandler(error);
            }
        },
    });
}

export function useMySubmissionsRequest(problemId: string) {
    const { user } = useAuthContext();
    return useQuery({
        queryKey: ['submission', problemId, user?.name],
        queryFn: async () => {
            if (!user) {
                throw new Error("User not logged in");
            }
            try {
                const response = await client.get<Submission[]>(`/api/v1/submissions/${problemId}/${user.name}`);
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

export function useCreateBundleMutation(createBundleRequest: CreateBundleRequest) {
    return useMutation({
        mutationKey: ['create-bundle', createBundleRequest],
        mutationFn: async () => {
            if (!createBundleRequest.name || createBundleRequest.problemIds.length == 0 || !createBundleRequest.description) {
                throw new Error("Invalid bundle request");
            }
            try {
                const response = await client.post<Bundle>(`/api/v1/bundles`, createBundleRequest);
                return response.data;
            } catch (error) {
                throw defaultErrorHandler(error);
            }
        }
    });
}

export function useBundleRequest(bundleCode: string | undefined) {
    return useQuery({
        queryKey: ['bundle', bundleCode],
        queryFn: async () => {
            if (!bundleCode) {
                return undefined;
            }
            try {
                const response = await client.get<Bundle>(`/api/v1/bundles/${bundleCode}`);
                return response.data;
            } catch (error) {
                throw defaultErrorHandler(error);
            }
        }
    });
}

export function useBundlesRequest(mode: "owned" | "public" | "joined") {
    return useQuery({
        queryKey: ['bundles', mode],
        queryFn: async () => {
            try {
                const response = await client.get<BundleMetadata[]>(`/api/v1/bundles/${mode}`);
                return response.data;
            } catch (error) {
                throw defaultErrorHandler(error);
            }
        }
    });
}

export function useOptionsRequest<T = string>(urlPath: string, prefix: string, size: number = 5) {
    return useQuery({
        queryKey: [urlPath, prefix, size],
        queryFn: async () => {
            try {
                const response = await client.get<T[]>(urlPath, {
                    params: { prefix: prefix, size: size, }
                });
                return response.data;
            } catch (error) {
                throw defaultErrorHandler(error);
            }
        }
    })
}

function defaultErrorHandler(error: any) {
    if (axios.isAxiosError(error)) {
        const status = fullStatus(error.response);
        return new Error(`Error fetching data: ${status}`);
    }
    return new Error(`An unknown error occurred. Please try again later.`);
}
