import { useMutation, useQuery } from "@tanstack/react-query";
import { whoami } from "@/generated/backend";
import { signIn, signOut, signUp } from "@/generated/gateway";
import { queryClient } from "@/lib/query-client";
import { queryKeys } from "@/lib/query-keys";

export function useWhoamiQuery() {
    return useQuery({
        queryKey: queryKeys.auth.whoami,
        queryFn: ({ signal }) => whoami(signal),
        staleTime: Infinity,
        retry: false,
        meta: { silent: true },
    });
}

export function useSignInMutation() {
    return useMutation({
        mutationFn: ({ username, password }: { username: string; password: string }) =>
            signIn({ username, password }),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: queryKeys.auth.whoami }).finally();
        },
    });
}

export function useSignUpMutation() {
    return useMutation({
        mutationFn: ({ username, password, email }: { username: string; password: string; email: string }) =>
            signUp({ username, password, email }),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: queryKeys.auth.whoami }).finally();
        },
    });
}

export function useSignOutMutation() {
    return useMutation({
        mutationFn: () => signOut(),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: queryKeys.auth.whoami }).finally();
        },
    });
}
