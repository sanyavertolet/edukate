import { useMutation, useQuery } from "@tanstack/react-query";
import { client } from "../client";
import { defaultErrorHandler } from "../utils";
import { useAuthContext } from "../../components/auth/AuthContextProvider";
import { CheckRequest } from "../../types/check/CheckRequest";
import { CheckResultInfo } from "../../types/check/CheckResultInfo";

export function useRequestCheckMutation() {
    const { isAuthorized } = useAuthContext();
    return useMutation({
        mutationKey: ['request-check'],
        mutationFn: async ({submissionId, checkType}: CheckRequest) => {
            if (!isAuthorized) {
                return null;
            }
            try {
                const response = await client.post<null>(`/api/v1/checker/${checkType}`, undefined, {
                    params: { id: submissionId }
                });
                return response.data;
            } catch (error) {
                throw defaultErrorHandler(error);
            }
        }
    });
}

export function useCheckResultsRequest(submissionId: string) {
    const { isAuthorized } = useAuthContext();
    return useQuery({
        queryKey: ['check-result-list', submissionId],
        queryFn: async () => {
            if (!isAuthorized) {
                return null;
            }
            try {
                const response = await client.get<CheckResultInfo[]>(`/api/v1/checker/submissions/${submissionId}`);
                return response.data;
            } catch (error) {
                throw defaultErrorHandler(error);
            }
        }
    })
}
