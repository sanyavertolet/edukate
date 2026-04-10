import { useMutation, useQuery } from "@tanstack/react-query";
import { aiCheck, getCheckResultsBySubmissionId, selfCheck, supervisorCheck } from "@/generated/backend";
import { useAuthContext } from "@/features/auth/context";
import { queryClient } from "@/lib/query-client";
import { queryKeys } from "@/lib/query-keys";
import { CheckRequest } from "./types";

const checkFn = {
    supervisor: supervisorCheck,
    self: selfCheck,
    ai: aiCheck,
};

export function useRequestCheckMutation() {
    return useMutation({
        mutationFn: ({ submissionId, checkType }: CheckRequest) => checkFn[checkType]({ id: submissionId }),
        onSuccess: (_data, { submissionId }) =>
            queryClient.invalidateQueries({ queryKey: queryKeys.checks.bySubmission(submissionId) }).finally(),
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
