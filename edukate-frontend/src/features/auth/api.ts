import { useMutation, useQuery } from "@tanstack/react-query";
import { client } from "@/lib/axios";
import { queryClient } from "@/lib/query-client";
import { queryKeys } from "@/lib/query-keys";
import { User } from "./types";

export function useWhoamiQuery() {
    return useQuery({
        queryKey: queryKeys.auth.whoami,
        queryFn: async () => {
            const response = await client.get("/api/v1/users/whoami");
            return response.status !== 401 ? (response.data as User) : null;
        },
        retry: false,
    });
}

interface SignInMutationParams {
    username: string;
    password: string;
}

export function useSignInMutation() {
    return useMutation({
        mutationFn: async (signInParams: SignInMutationParams) => {
            const response = await client.post("/api/v1/auth/sign-in", signInParams);
            return response.status === 204;
        },
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: queryKeys.auth.whoami }).finally();
        },
    });
}

interface SignUpMutationParams {
    username: string;
    password: string;
    email: string;
}

export function useSignUpMutation() {
    return useMutation({
        mutationFn: async (signUpParams: SignUpMutationParams) => {
            const response = await client.post("/api/v1/auth/sign-up", signUpParams);
            return response.status === 204;
        },
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: queryKeys.auth.whoami }).finally();
        },
    });
}

export function useSignOutMutation() {
    return useMutation({
        mutationFn: async () => {
            const response = await client.post("/api/v1/auth/sign-out");
            return response.status === 204;
        },
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: queryKeys.auth.whoami }).finally();
        },
    });
}
