import { useMutation, useQuery } from "@tanstack/react-query";
import {
    changeUserRole,
    ChangeUserRoleRequestedRole,
    createBundle,
    expireInvite,
    getBundleByShareCode,
    getInvitedUsers,
    getJoinedBundles,
    getOwnedBundles,
    getPublicBundles,
    getUserRoles,
    inviteToBundle,
    joinBundle,
    replyToInvite,
} from "@/generated/backend";
import { useAuthContext } from "@/features/auth/context";
import { queryClient } from "@/lib/query-client";
import { queryKeys } from "@/lib/query-keys";
import { BundleCategory, CreateBundleRequest } from "./types";

export function useCreateBundleMutation(createBundleRequest: CreateBundleRequest) {
    return useMutation({
        mutationFn: () => {
            if (
                !createBundleRequest.name ||
                createBundleRequest.problemIds.length == 0 ||
                !createBundleRequest.description
            ) {
                throw new Error("Invalid bundle request");
            }
            return createBundle(createBundleRequest);
        },
        onSuccess: () =>
            queryClient.invalidateQueries({ queryKey: queryKeys.bundles.list("owned") }).finally(),
    });
}

export function useBundleRequest(bundleCode: string | undefined) {
    return useQuery({
        queryKey: queryKeys.bundles.detail(bundleCode ?? ""),
        queryFn: ({ signal }) => getBundleByShareCode(bundleCode!, signal),
        enabled: !!bundleCode,
    });
}

const bundleListFn: Record<BundleCategory, typeof getPublicBundles> = {
    public: getPublicBundles,
    owned: getOwnedBundles,
    joined: getJoinedBundles,
};

export function useBundlesRequest(category: BundleCategory) {
    return useQuery({
        queryKey: queryKeys.bundles.list(category),
        queryFn: ({ signal }) => bundleListFn[category](undefined, signal),
    });
}

export function useJoinBundleMutation() {
    return useMutation({
        mutationFn: (shareCode: string) => joinBundle(shareCode),
        onSuccess: (_data, shareCode) => {
            queryClient.invalidateQueries({ queryKey: queryKeys.bundles.list("joined") }).finally();
            queryClient.invalidateQueries({ queryKey: queryKeys.bundles.detail(shareCode) }).finally();
        },
    });
}

export function useBundleInviteUserMutation() {
    return useMutation({
        mutationFn: ({ username, shareCode }: { username: string; shareCode: string }) =>
            inviteToBundle(shareCode, { inviteeName: username }),
        onSuccess: (_data, { shareCode }) =>
            queryClient.invalidateQueries({ queryKey: queryKeys.bundles.invitedUsers(shareCode) }).finally(),
    });
}

export function useBundleInvitationReplyMutation() {
    return useMutation({
        mutationFn: ({ shareCode, isAccepted }: { shareCode: string; isAccepted: boolean }) =>
            replyToInvite(shareCode, { response: isAccepted }),
        onSuccess: (_data, { shareCode }) => {
            queryClient.invalidateQueries({ queryKey: queryKeys.bundles.list("joined") }).finally();
            queryClient.invalidateQueries({ queryKey: queryKeys.bundles.detail(shareCode) }).finally();
        },
    });
}

export function useBundleChangeUserRoleMutation() {
    return useMutation({
        mutationFn: ({ username, role, shareCode }: { username: string; role: string; shareCode: string }) =>
            changeUserRole(shareCode, { username, requestedRole: role as ChangeUserRoleRequestedRole }),
        onSuccess: (_data, { shareCode }) =>
            queryClient.invalidateQueries({ queryKey: queryKeys.bundles.users(shareCode) }).finally(),
    });
}

export function useBundleUserListQuery(shareCode: string) {
    const { isAuthorized } = useAuthContext();
    return useQuery({
        queryKey: queryKeys.bundles.users(shareCode),
        enabled: isAuthorized,
        queryFn: ({ signal }) => getUserRoles(shareCode, signal),
    });
}

export function useBundleInvitedUserListQuery(shareCode: string) {
    const { isAuthorized } = useAuthContext();
    return useQuery({
        queryKey: queryKeys.bundles.invitedUsers(shareCode),
        enabled: isAuthorized,
        queryFn: ({ signal }) => getInvitedUsers(shareCode, signal),
    });
}

export function useBundleExpireInviteMutation() {
    return useMutation({
        mutationFn: ({ shareCode, username }: { shareCode: string; username: string }) =>
            expireInvite(shareCode, { inviteeName: username }),
        onSuccess: (_data, { shareCode }) =>
            queryClient.invalidateQueries({ queryKey: queryKeys.bundles.invitedUsers(shareCode) }).finally(),
    });
}
