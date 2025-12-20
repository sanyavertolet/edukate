import { useMutation, useQuery } from "@tanstack/react-query";
import { useAuthContext } from "../../components/auth/AuthContextProvider";
import { defaultErrorHandler } from "../utils";
import { client } from "../client";
import { UserNameWithRole } from "../../types/user/UserNameWithRole";
import { CreateBundleRequest } from "../../types/bundle/CreateBundleRequest";
import { Bundle, BundleCategory } from "../../types/bundle/Bundle";
import { BundleMetadata } from "../../types/bundle/BundleMetadata";

export function useCreateBundleMutation(createBundleRequest: CreateBundleRequest) {
    return useMutation({
        mutationKey: ['create-bundle', createBundleRequest],
        mutationFn: async () => {
            if (!createBundleRequest.name || createBundleRequest.problemIds.length == 0 || !createBundleRequest.description) {
                throw new Error("Invalid bundle request");
            }
            try {
                const response = await client.post<Bundle>(`/api/v1/bundles`, createBundleRequest);
                return response.data;
            } catch (error) {
                throw defaultErrorHandler(error);
            }
        }
    });
}

export function useBundleRequest(bundleCode: string | undefined) {
    return useQuery({
        queryKey: ['bundle', bundleCode],
        queryFn: async () => {
            if (!bundleCode) {
                return undefined;
            }
            try {
                const response = await client.get<Bundle>(`/api/v1/bundles/${bundleCode}`);
                return response.data;
            } catch (error) {
                throw defaultErrorHandler(error);
            }
        }
    });
}

export function useBundlesRequest(category: BundleCategory) {
    return useQuery({
        queryKey: ['bundles', category],
        queryFn: async () => {
            try {
                const response = await client.get<BundleMetadata[]>(`/api/v1/bundles/${category}`);
                return response.data;
            } catch (error) {
                throw defaultErrorHandler(error);
            }
        }
    });
}

export function useJoinBundleMutation() {
    const { isAuthorized } = useAuthContext();
    return useMutation({
        mutationKey: ['join-bundle'],
        mutationFn: async (bundleCode: string) => {
            if (!isAuthorized) {
                return null;
            }
            try {
                const response = await client.post<BundleMetadata>(`/api/v1/bundles/${bundleCode}/join`);
                return response.data;
            } catch (error) {
                throw defaultErrorHandler(error);
            }
        }
    })
}


export function useBundleInviteUserMutation() {
    const { isAuthorized } = useAuthContext();
    return useMutation({
        mutationKey: ['bundle', 'user', 'invite'],
        mutationFn: async ({username, shareCode}: {username: string, shareCode: string}) => {
            if (!isAuthorized) {
                return null;
            }
            try {
                const response = await client.post<string>(`/api/v1/bundles/${shareCode}/invite`, undefined, {
                    params: { shareCode, inviteeName: username }
                });
                return response.status;
            } catch (error) {
                throw defaultErrorHandler(error);
            }
        }
    })
}

export function useBundleInvitationReplyMutation() {
    const { isAuthorized } = useAuthContext();
    return useMutation({
        mutationKey: ['bundle', 'user', 'reply-invite'],
        mutationFn: async ({ shareCode, isAccepted }: { shareCode: string, isAccepted: boolean }) => {
            if (!isAuthorized) {
                return null;
            }
            const response = await client.post<string>(`/api/v1/bundles/${shareCode}/reply-invite`, undefined, {
                params: { shareCode, response: isAccepted }
            });
            return response.status;
        }
    })
}

export function useBundleChangeUserRoleMutation() {
    const { isAuthorized } = useAuthContext();
    return useMutation({
        mutationKey: ['bundle', 'user', 'role'],
        mutationFn: async ({username, role, shareCode} : {username: string, role: string, shareCode: string}) => {
            if (!isAuthorized) {
                return null;
            }
            try {
                const response = await client.post<string>(`/api/v1/bundles/${shareCode}/role`, undefined, {
                    params: { username, requestedRole: role }
                });
                return response.status;
            } catch (error) {
                throw defaultErrorHandler(error);
            }
        }
    })
}

export function useBundleUserListQuery(shareCode: string) {
    const { isAuthorized } = useAuthContext();
    return useQuery({
        queryKey: ['bundle', 'user', 'list', shareCode, isAuthorized],
        queryFn: async () => {
            if (!isAuthorized) {
                return null;
            }
            try {
                const response = await client.get<UserNameWithRole[]>(`/api/v1/bundles/${shareCode}/users`);
                return response.data;
            } catch (error) {
                throw defaultErrorHandler(error);
            }
        }
    })
}

export function useBundleInvitedUserListQuery(shareCode: string) {
    const { isAuthorized } = useAuthContext();
    return useQuery({
        queryKey: ['bundle', 'invited-user', 'list', shareCode, isAuthorized],
        queryFn: async () => {
            if (!isAuthorized) {
                return null;
            }
            try {
                const response = await client.get<string[]>(`/api/v1/bundles/${shareCode}/invited-users`);
                return response.data;
            } catch (error) {
                throw defaultErrorHandler(error);
            }
        }
    })
}

export function useBundleExpireInviteMutation() {
    const { isAuthorized } = useAuthContext();
    return useMutation({
        mutationKey: ['bundle', 'user', 'expire-invite'],
        mutationFn: async ({ shareCode, username }: { shareCode: string, username: string }) => {
            if (!isAuthorized) {
                return null;
            }
            try {
                const response = await client.post<string>(`/api/v1/bundles/${shareCode}/expire-invite`, undefined, {
                    params: { shareCode, inviteeName: username }
                });
                return response.status;
            } catch (error) {
                throw defaultErrorHandler(error);
            }
        }
    })
}


