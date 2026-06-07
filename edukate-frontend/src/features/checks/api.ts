import { useMutation, useQuery } from "@tanstack/react-query";
import { aiCheck, getCheckResultById, getCheckResultsBySubmissionId, selfCheck, supervisorCheck } from "@/generated/backend";
import { useAuthContext } from "@/features/auth/context";
import { queryClient } from "@/lib/query-client";
import { queryKeys } from "@/lib/query-keys";
import { CheckRequest } from "./types";

export function useRequestCheckMutation() {
    return useMutation({
        mutationFn: (req: CheckRequest) => {
            if (req.checkType === "supervisor") {
                return supervisorCheck({
                    submissionId: Number(req.submissionId),
                    problemSetCode: req.problemSetCode,
                    supervisorName: req.supervisorName,
                });
            }
            return req.checkType === "ai" ? aiCheck({ id: req.submissionId }) : selfCheck({ id: req.submissionId });
        },
        onSuccess: (_data, req) => {
            void queryClient.invalidateQueries({ queryKey: queryKeys.checks.bySubmission(req.submissionId) });
            void queryClient.invalidateQueries({ queryKey: queryKeys.submissions.byProblem(req.problemKey) });
            void queryClient.invalidateQueries({ queryKey: queryKeys.problems.detail(req.problemKey) });
        },
    });
}

export function useCheckResultsRequest(submissionId: string) {
    const { isAuthorized } = useAuthContext();
    return useQuery({
        queryKey: queryKeys.checks.bySubmission(submissionId),
        enabled: isAuthorized,
        queryFn: ({ signal }) => getCheckResultsBySubmissionId(submissionId, signal),
    });
}

export function useCheckResultDetailQuery(checkResultId: number | null) {
    const { isAuthorized } = useAuthContext();
    return useQuery({
        queryKey: queryKeys.checks.detail(String(checkResultId ?? "")),
        enabled: isAuthorized && checkResultId !== null,
        queryFn: ({ signal }) => getCheckResultById(String(checkResultId ?? ""), signal),
    });
}
