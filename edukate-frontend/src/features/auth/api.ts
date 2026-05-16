import { useMutation, useQuery } from "@tanstack/react-query";
import { whoami } from "@/generated/backend";
import { forgotPassword, resetPassword, signIn, signOut, signUp } from "@/generated/gateway";
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
        mutationFn: ({ username, password }: { username: string; password: string }) => signIn({ username, password }),
        onSuccess: () => {
            void queryClient.invalidateQueries({ queryKey: queryKeys.auth.whoami });
        },
        meta: { silent: true },
    });
}

export function useSignUpMutation() {
    return useMutation({
        mutationFn: ({ username, password, email }: { username: string; password: string; email: string }) =>
            signUp({ username, password, email }),
        onSuccess: () => {
            void queryClient.invalidateQueries({ queryKey: queryKeys.auth.whoami });
        },
    });
}

export function useSignOutMutation() {
    return useMutation({
        mutationFn: () => signOut(),
        onSuccess: () => {
            void queryClient.invalidateQueries({ queryKey: queryKeys.auth.whoami });
        },
    });
}

export function useForgotPasswordMutation() {
    return useMutation({
        mutationFn: ({ email }: { email: string }) => forgotPassword({ email }),
    });
}

export function useResetPasswordMutation() {
    return useMutation({
        mutationFn: ({ token, newPassword }: { token: string; newPassword: string }) =>
            resetPassword({ token, newPassword }),
    });
}
