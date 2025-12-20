import { useMutation, useQuery } from "@tanstack/react-query";
import { defaultErrorHandler } from "../utils";
import { client } from "../client";
import { Submission } from "../../types/submission/Submission";
import { CreateSubmissionRequest } from "../../types/submission/CreateSubmissionRequest";
import { useAuthContext } from "../../components/auth/AuthContextProvider";

export function useMySubmissionsQuery(problemId?: string) {
    const { isAuthorized } = useAuthContext();
    return useQuery({
        queryKey: ['problem-submissions', 'my', problemId],
        queryFn: async () => {
            if (!isAuthorized) {
                return null;
            }
            try {
                const params = problemId ? { problemId } : undefined;
                const response = await client.get<Submission[]>(`/api/v1/submissions/my`, {
                    params: params
                });
                return response.data;
            } catch (error) {
                throw defaultErrorHandler(error);
            }
        }
    })
}

export function useSubmitProblemMutation() {
    const { user } = useAuthContext();
    return useMutation({
        mutationKey: ['submission', user],
        mutationFn: async (request: CreateSubmissionRequest) => {
            if (!user) {
                throw new Error("User not signed in");
            }
            try {
                const response = await client.post<Submission>('/api/v1/submissions', request);
                return response.data;
            } catch (error) {
                throw defaultErrorHandler(error);
            }
        },
    });
}
