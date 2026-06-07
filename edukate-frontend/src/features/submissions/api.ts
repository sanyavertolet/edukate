import { useMutation, useQuery } from "@tanstack/react-query";
import {
    getMySubmissions,
    getSubmissionById,
    searchSubmissions,
    SearchSubmissionsStatus,
    uploadSubmission,
} from "@/generated/backend";
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
            queryClient.removeQueries({ queryKey: queryKeys.files.all });
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

export function useSubmissionSearchQuery(
    page: number,
    size: number,
    userPrefix?: string,
    bookSlugPrefix?: string,
    problemCodePrefix?: string,
    status?: string,
) {
    const { isAuthorized } = useAuthContext();
    return useQuery({
        queryKey: queryKeys.submissions.search(page, size, userPrefix, bookSlugPrefix, problemCodePrefix, status),
        queryFn: ({ signal }) =>
            searchSubmissions(
                {
                    page,
                    size,
                    userPrefix: userPrefix || undefined,
                    bookSlugPrefix: bookSlugPrefix || undefined,
                    problemCodePrefix: problemCodePrefix || undefined,
                    status: status as SearchSubmissionsStatus | undefined,
                },
                signal,
            ),
        enabled: isAuthorized,
    });
}
