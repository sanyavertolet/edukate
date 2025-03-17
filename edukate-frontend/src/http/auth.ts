import { useMutation, useQuery } from "@tanstack/react-query";
import { client } from "./client";
import { AuthorizationInfo } from "../types/AuthorizationInfo";
import { useAuthContext } from "../components/auth/AuthContextProvider";
import { User } from "../types/User";
import { useCookies } from "react-cookie";
import { defaultCookieOptions, TOKEN_COOKIE } from "../utils/cookies";

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
            } catch (e) {
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
    const { setUser } = useAuthContext();
    const [ _, setCookies ] = useCookies([TOKEN_COOKIE]);
    return useMutation({
        mutationFn: async (signInParams: SignInMutationParams) => {
            const response = await client.post('/auth/sign-in', signInParams);
            return response.data as AuthorizationInfo;
        },
        onSuccess: (data) => {
            setCookies(TOKEN_COOKIE, data.token, defaultCookieOptions);
            setUser({ name: data.username, roles: data.roles, status: data.status });
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
    const { setUser } = useAuthContext();
    const [ _, setCookies ] = useCookies([TOKEN_COOKIE]);
    return useMutation({
        mutationFn: async (signUpParams: SignUpMutationParams) => {
            const response = await client.post('/auth/sign-up', signUpParams);
            return response.data as AuthorizationInfo;
        },
        onSuccess: (data) => {
            setCookies(TOKEN_COOKIE, data.token, defaultCookieOptions);
            setUser({ name: data.username, roles: data.roles, status: data.status });
            onSuccess();
        },
    });
}

export const useSignOut = () => {
    const { setUser } = useAuthContext();
    const [ , , removeCookie ] = useCookies([TOKEN_COOKIE]);

    return () => {
        removeCookie(TOKEN_COOKIE, defaultCookieOptions);
        setUser(undefined);
    };
}
