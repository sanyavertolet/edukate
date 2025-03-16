import { useMutation, useQuery } from "@tanstack/react-query";
import { ProblemMetadata } from "../types/ProblemMetadata";
import { client } from "./client";
import axios from "axios";
import { Problem } from "../types/Problem";
import { Submission } from "../types/Submission";
import { useAuthContext } from "../components/auth/AuthContextProvider";

export function useProblemListRequest(page: number, size: number) {
    const { user } = useAuthContext();
    return useQuery({
        queryKey: ['problemList', page, size, user],
        queryFn: async () => {
            try {
                const response = await client.get(`/api/v1/problems?page=${page}&size=${size}`);
                return response.data as ProblemMetadata[];
            } catch (error) {
                if (axios.isAxiosError(error)) {
                    const status = error.response?.status;
                    const message = error.response?.data?.message;
                    throw new Error(`Error fetching data: ${status} - ${message}`);
                }
                throw new Error(`An unknown error occurred. Please try again later.`);
            }
        },
    });
}

export function useProblemCountRequest() {
    return useQuery({
        queryKey: ['problemCount'],
        queryFn: async () => {
            try {
                const response = await client.get('/api/v1/problems/count');
                return response.data as number;
            } catch (error) {
                if (axios.isAxiosError(error)) {
                    const status = error.response?.status;
                    const message = error.response?.data?.message;
                    throw new Error(`Error fetching data: ${status} - ${message}`);
                }
                throw new Error(`An unknown error occurred. Please try again later.`);
            }
        },
    });
}

export function useProblemRequest(id: string) {
    const { user } = useAuthContext();
    return useQuery({
        queryKey: ['problem', id, user],
        queryFn: async () => {
            try {
                const response = await client.get<Problem>(`/api/v1/problems/${id}`);
                return response.data;
            } catch (error) {
                if (axios.isAxiosError(error)) {
                    const status = error.response?.status;
                    throw new Error(`Error fetching data: ${status}`);
                }
                throw error;
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
                return response.data as Submission;
            } catch (error) {
                if (axios.isAxiosError(error)) {
                    const status = error.response?.status;
                    throw new Error(`Error fetching data: ${status}`);
                }
                throw error;
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
                return response.data as Submission[];
            } catch (error) {
                if (axios.isAxiosError(error)) {
                    const status = error.response?.status;
                    throw new Error(`Error fetching data: ${status}`);
                }
                throw error;
            }
        },
    });
}
