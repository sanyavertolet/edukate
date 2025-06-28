import { useMutation, useQuery } from "@tanstack/react-query";
import { client } from "./client";
import { AuthorizationInfo } from "../types/user/AuthorizationInfo";
import { User } from "../types/user/User";
import { useCookies } from "react-cookie";
import { defaultCookieOptions, TOKEN_COOKIE } from "../utils/cookies";
import { queryClient } from "./queryClient";

export function useWhoamiQuery() {
    const [ cookies ] = useCookies([TOKEN_COOKIE]);
    return useQuery({
        queryKey: ['whoami', cookies[TOKEN_COOKIE]],
        queryFn: async () => {
            if (!cookies[TOKEN_COOKIE]) {
                return null;
            }
            try {
                const response = await client.get('/api/v1/users/whoami');
                return response.data as User;
            } catch {
                return null;
            }
        },
        retry: false,
    });
}

interface SignInMutationParams {
    username: string,
    password: string,
}

export function useSignInMutation(onSuccess: () => void = () => {}) {
    const [, setCookies] = useCookies([TOKEN_COOKIE]);
    return useMutation({
        mutationFn: async (signInParams: SignInMutationParams) => {
            const response = await client.post('/auth/sign-in', signInParams);
            return response.data as AuthorizationInfo;
        },
        onSuccess: (data) => {
            setCookies(TOKEN_COOKIE, data.token, defaultCookieOptions);
            queryClient.invalidateQueries({ queryKey: ['whoami'] }).finally();
            onSuccess();
        },
    });
}

interface SignUpMutationParams {
    username: string,
    password: string,
    email: string,
}

export function useSignUpMutation(onSuccess: () => void = () => {}) {
    const [, setCookies] = useCookies([TOKEN_COOKIE]);
    return useMutation({
        mutationFn: async (signUpParams: SignUpMutationParams) => {
            const response = await client.post('/auth/sign-up', signUpParams);
            return response.data as AuthorizationInfo;
        },
        onSuccess: (data) => {
            setCookies(TOKEN_COOKIE, data.token, defaultCookieOptions);
            queryClient.invalidateQueries({ queryKey: ['whoami'] }).finally();
            onSuccess();
        },
    });
}

/**
 * Hook for signing out the current user
 * @returns A function that signs out the user
 */
export const useSignOut = () => {
    const [, , removeCookie] = useCookies([TOKEN_COOKIE]);

    return () => {
        removeCookie(TOKEN_COOKIE, defaultCookieOptions);
        queryClient.invalidateQueries({ queryKey: ['whoami'] }).finally();
    };
}
