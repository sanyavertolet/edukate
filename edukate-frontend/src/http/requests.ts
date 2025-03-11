import { useQuery } from "@tanstack/react-query";
import { ProblemMetadata } from "../types/ProblemMetadata";
import { client } from "./client";
import axios from "axios";
import { Problem } from "../types/Problem";

export function useProblemListRequest() {
    return useQuery({
        queryKey: ['problemList'],
        queryFn: async () => {
            try {
                const response = await client.get('/api/v1/problems');
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

export function useProblemRequest(id: string) {
    return useQuery({
        queryKey: ['problem', id],
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

