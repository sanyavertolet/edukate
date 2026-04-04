import { useMutation, useQuery } from "@tanstack/react-query";
import { client } from "@/lib/axios";
import { defaultErrorHandler } from "@/lib/error-handler";
import { useAuthContext } from "@/features/auth/context";
import { queryKeys } from "@/lib/query-keys";
import { CheckRequest, CheckResultInfo } from "./types";

export function useRequestCheckMutation() {
    return useMutation({
        mutationKey: ["request-check"],
        mutationFn: async ({ submissionId, checkType }: CheckRequest) => {
            try {
                const response = await client.post<null>(`/api/v1/checker/${checkType}`, undefined, {
                    params: { id: submissionId },
                });
                return response.data;
            } catch (error) {
                throw defaultErrorHandler(error);
            }
        },
    });
}

export function useCheckResultsRequest(submissionId: string) {
    const { isAuthorized } = useAuthContext();
    return useQuery({
        queryKey: queryKeys.checks.bySubmission(submissionId),
        enabled: isAuthorized,
        queryFn: async () => {
            try {
                const response = await client.get<CheckResultInfo[]>(`/api/v1/checker/submissions/${submissionId}`);
                return response.data;
            } catch (error) {
                throw defaultErrorHandler(error);
            }
        },
    });
}
