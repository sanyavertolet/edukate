import { useMutation, useQuery } from "@tanstack/react-query";
import { useAuthContext } from "@/features/auth/context";
import { defaultErrorHandler } from "@/lib/error-handler";
import { client } from "@/lib/axios";
import { queryKeys } from "@/lib/query-keys";
import { Bundle, BundleCategory, BundleMetadata, CreateBundleRequest } from "./types";
import { UserNameWithRole } from "@/features/auth/types";

export function useCreateBundleMutation(createBundleRequest: CreateBundleRequest) {
    return useMutation({
        mutationKey: queryKeys.bundles.all,
        mutationFn: async () => {
            if (
                !createBundleRequest.name ||
                createBundleRequest.problemIds.length == 0 ||
                !createBundleRequest.description
            ) {
                throw new Error("Invalid bundle request");
            }
            try {
                const response = await client.post<Bundle>(`/api/v1/bundles`, createBundleRequest);
                return response.data;
            } catch (error) {
                throw defaultErrorHandler(error);
            }
        },
    });
}

export function useBundleRequest(bundleCode: string | undefined) {
    return useQuery({
        queryKey: queryKeys.bundles.detail(bundleCode ?? ""),
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
        },
    });
}

export function useBundlesRequest(category: BundleCategory) {
    return useQuery({
        queryKey: queryKeys.bundles.list(category),
        queryFn: async () => {
            try {
                const response = await client.get<BundleMetadata[]>(`/api/v1/bundles/${category}`);
                return response.data;
            } catch (error) {
                throw defaultErrorHandler(error);
            }
        },
    });
}

export function useJoinBundleMutation() {
    return useMutation({
        mutationKey: ["join-bundle"],
        mutationFn: async (bundleCode: string) => {
            try {
                const response = await client.post<BundleMetadata>(`/api/v1/bundles/${bundleCode}/join`);
                return response.data;
            } catch (error) {
                throw defaultErrorHandler(error);
            }
        },
    });
}

export function useBundleInviteUserMutation() {
    return useMutation({
        mutationKey: ["bundle", "user", "invite"],
        mutationFn: async ({ username, shareCode }: { username: string; shareCode: string }) => {
            try {
                const response = await client.post<string>(`/api/v1/bundles/${shareCode}/invite`, undefined, {
                    params: { shareCode, inviteeName: username },
                });
                return response.status;
            } catch (error) {
                throw defaultErrorHandler(error);
            }
        },
    });
}

export function useBundleInvitationReplyMutation() {
    return useMutation({
        mutationKey: ["bundle", "user", "reply-invite"],
        mutationFn: async ({ shareCode, isAccepted }: { shareCode: string; isAccepted: boolean }) => {
            const response = await client.post<string>(`/api/v1/bundles/${shareCode}/reply-invite`, undefined, {
                params: { shareCode, response: isAccepted },
            });
            return response.status;
        },
    });
}

export function useBundleChangeUserRoleMutation() {
    return useMutation({
        mutationKey: ["bundle", "user", "role"],
        mutationFn: async ({ username, role, shareCode }: { username: string; role: string; shareCode: string }) => {
            try {
                const response = await client.post<string>(`/api/v1/bundles/${shareCode}/role`, undefined, {
                    params: { username, requestedRole: role },
                });
                return response.status;
            } catch (error) {
                throw defaultErrorHandler(error);
            }
        },
    });
}

export function useBundleUserListQuery(shareCode: string) {
    const { isAuthorized } = useAuthContext();
    return useQuery({
        queryKey: queryKeys.bundles.users(shareCode),
        enabled: isAuthorized,
        queryFn: async () => {
            try {
                const response = await client.get<UserNameWithRole[]>(`/api/v1/bundles/${shareCode}/users`);
                return response.data;
            } catch (error) {
                throw defaultErrorHandler(error);
            }
        },
    });
}

export function useBundleInvitedUserListQuery(shareCode: string) {
    const { isAuthorized } = useAuthContext();
    return useQuery({
        queryKey: queryKeys.bundles.invitedUsers(shareCode),
        enabled: isAuthorized,
        queryFn: async () => {
            try {
                const response = await client.get<string[]>(`/api/v1/bundles/${shareCode}/invited-users`);
                return response.data;
            } catch (error) {
                throw defaultErrorHandler(error);
            }
        },
    });
}

export function useBundleExpireInviteMutation() {
    return useMutation({
        mutationKey: ["bundle", "user", "expire-invite"],
        mutationFn: async ({ shareCode, username }: { shareCode: string; username: string }) => {
            try {
                const response = await client.post<string>(`/api/v1/bundles/${shareCode}/expire-invite`, undefined, {
                    params: { shareCode, inviteeName: username },
                });
                return response.status;
            } catch (error) {
                throw defaultErrorHandler(error);
            }
        },
    });
}
