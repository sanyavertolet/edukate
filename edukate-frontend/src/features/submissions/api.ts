import { useMutation, useQuery } from "@tanstack/react-query";
import { defaultErrorHandler } from "@/lib/error-handler";
import { client } from "@/lib/axios";
import { queryKeys } from "@/lib/query-keys";
import { useAuthContext } from "@/features/auth/context";
import { Submission, CreateSubmissionRequest } from "./types";

export function useSubmitProblemMutation() {
    return useMutation({
        mutationKey: queryKeys.submissions.all,
        mutationFn: async (request: CreateSubmissionRequest) => {
            try {
                const response = await client.post<Submission>("/api/v1/submissions", request);
                return response.data;
            } catch (error) {
                throw defaultErrorHandler(error);
            }
        },
    });
}

export function useMySubmissionsQuery(problemId?: string) {
    const { isAuthorized } = useAuthContext();
    return useQuery({
        queryKey: queryKeys.submissions.byProblem(problemId ?? ""),
        enabled: isAuthorized,
        queryFn: async () => {
            try {
                const params = problemId ? { problemId } : undefined;
                const response = await client.get<Submission[]>(`/api/v1/submissions/my`, {
                    params: params,
                });
                return response.data;
            } catch (error) {
                throw defaultErrorHandler(error);
            }
        },
    });
}

export function useSubmissionQuery(submissionId: string | undefined) {
    return useQuery({
        queryKey: queryKeys.submissions.detail(submissionId ?? ""),
        queryFn: async () => {
            if (!submissionId) {
                return null;
            }
            try {
                const response = await client.get<Submission>(`/api/v1/submissions/by-id/${submissionId}`);
                return response.data;
            } catch (error) {
                throw defaultErrorHandler(error);
            }
        },
    });
}
