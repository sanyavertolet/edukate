import { useMutation, useQuery } from "@tanstack/react-query";
import { getMySubmissions, getSubmissionById, uploadSubmission } from "@/generated/backend";
import { queryClient } from "@/lib/query-client";
import { queryKeys } from "@/lib/query-keys";
import { useAuthContext } from "@/features/auth/context";
import { CreateSubmissionRequest } from "./types";

export function useSubmitProblemMutation() {
    return useMutation({
        mutationFn: (request: CreateSubmissionRequest) => uploadSubmission(request),
        onSuccess: (_data, { problemId }) => {
            void queryClient.invalidateQueries({ queryKey: queryKeys.submissions.byProblem(problemId) });
            void queryClient.invalidateQueries({ queryKey: queryKeys.problems.detail(problemId) });
        },
    });
}

export function useMySubmissionsQuery(problemId?: string) {
    const { isAuthorized } = useAuthContext();
    return useQuery({
        queryKey: queryKeys.submissions.byProblem(problemId ?? ""),
        enabled: isAuthorized,
        queryFn: ({ signal }) => getMySubmissions({ problemId }, signal),
    });
}

export function useSubmissionQuery(submissionId: string | undefined) {
    return useQuery({
        queryKey: queryKeys.submissions.detail(submissionId ?? ""),
        queryFn: ({ signal }) => getSubmissionById(submissionId as string, signal),
        enabled: !!submissionId,
    });
}
