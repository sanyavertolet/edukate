import { useMutation, useQuery } from "@tanstack/react-query";
import { getMySubmissions, getSubmissionById, uploadSubmission } from "@/generated/backend";
import { queryClient } from "@/lib/query-client";
import { queryKeys } from "@/lib/query-keys";
import { useAuthContext } from "@/features/auth/context";
import { CreateSubmissionRequest } from "./types";

export function useSubmitProblemMutation() {
    return useMutation({
        mutationFn: (request: CreateSubmissionRequest) => uploadSubmission(request),
        onSuccess: (_data, { problemKey }) => {
            void queryClient.invalidateQueries({ queryKey: queryKeys.submissions.byProblem(problemKey) });
            void queryClient.invalidateQueries({ queryKey: queryKeys.problems.detail(problemKey) });
            void queryClient.invalidateQueries({ queryKey: queryKeys.files.temp });
        },
    });
}

export function useMySubmissionsQuery(problemKey?: string) {
    const { isAuthorized } = useAuthContext();
    return useQuery({
        queryKey: queryKeys.submissions.byProblem(problemKey ?? ""),
        enabled: isAuthorized,
        queryFn: ({ signal }) => getMySubmissions({ problemKey }, signal),
    });
}

export function useSubmissionQuery(submissionId: string | undefined) {
    return useQuery({
        queryKey: queryKeys.submissions.detail(submissionId ?? ""),
        queryFn: ({ signal }) => getSubmissionById(Number(submissionId), signal),
        enabled: !!submissionId,
    });
}
