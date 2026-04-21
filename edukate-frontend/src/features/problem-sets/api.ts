import { useMutation, useQuery } from "@tanstack/react-query";
import {
    changeUserRole,
    ChangeUserRoleRequestedRole,
    createProblemSet,
    expireInvite,
    getProblemSetByShareCode,
    getInvitedUsers,
    getJoinedProblemSets,
    getOwnedProblemSets,
    getPublicProblemSets,
    getUserRoles,
    inviteToProblemSet,
    replyToInvite,
} from "@/generated/backend";
import { useAuthContext } from "@/features/auth/context";
import { queryClient } from "@/lib/query-client";
import { queryKeys } from "@/lib/query-keys";
import { ProblemSetCategory, CreateProblemSetRequest } from "./types";

export function useCreateProblemSetMutation(createProblemSetRequest: CreateProblemSetRequest) {
    return useMutation({
        mutationFn: () => {
            if (
                !createProblemSetRequest.name ||
                createProblemSetRequest.problemKeys.length == 0 ||
                !createProblemSetRequest.description
            ) {
                throw new Error("Invalid problem set request");
            }
            return createProblemSet(createProblemSetRequest);
        },
        onSuccess: () => void queryClient.invalidateQueries({ queryKey: queryKeys.problemSets.list("owned") }),
    });
}

export function useProblemSetRequest(problemSetCode: string | undefined) {
    return useQuery({
        queryKey: queryKeys.problemSets.detail(problemSetCode ?? ""),
        queryFn: ({ signal }) => getProblemSetByShareCode(problemSetCode as string, signal),
        enabled: !!problemSetCode,
    });
}

const problemSetListFn: Record<ProblemSetCategory, typeof getPublicProblemSets> = {
    public: getPublicProblemSets,
    owned: getOwnedProblemSets,
    joined: getJoinedProblemSets,
};

export function useProblemSetsRequest(category: ProblemSetCategory) {
    return useQuery({
        queryKey: queryKeys.problemSets.list(category),
        queryFn: ({ signal }) => problemSetListFn[category](undefined, signal),
    });
}

export function useProblemSetInviteUserMutation() {
    return useMutation({
        mutationFn: ({ username, shareCode }: { username: string; shareCode: string }) =>
            inviteToProblemSet(shareCode, { inviteeName: username }),
        onSuccess: (_data, { shareCode }) =>
            void queryClient.invalidateQueries({ queryKey: queryKeys.problemSets.invitedUsers(shareCode) }),
    });
}

export function useProblemSetInvitationReplyMutation() {
    return useMutation({
        mutationFn: ({ shareCode, isAccepted }: { shareCode: string; isAccepted: boolean }) =>
            replyToInvite(shareCode, { response: isAccepted }),
        onSuccess: (_data, { shareCode }) => {
            void queryClient.invalidateQueries({ queryKey: queryKeys.problemSets.list("joined") });
            void queryClient.invalidateQueries({ queryKey: queryKeys.problemSets.detail(shareCode) });
        },
    });
}

export function useProblemSetChangeUserRoleMutation() {
    return useMutation({
        mutationFn: ({ username, role, shareCode }: { username: string; role: string; shareCode: string }) =>
            changeUserRole(shareCode, { username, requestedRole: role as ChangeUserRoleRequestedRole }),
        onSuccess: (_data, { shareCode }) =>
            void queryClient.invalidateQueries({ queryKey: queryKeys.problemSets.users(shareCode) }),
    });
}

export function useProblemSetUserListQuery(shareCode: string) {
    const { isAuthorized } = useAuthContext();
    return useQuery({
        queryKey: queryKeys.problemSets.users(shareCode),
        enabled: isAuthorized,
        queryFn: ({ signal }) => getUserRoles(shareCode, signal),
    });
}

export function useProblemSetInvitedUserListQuery(shareCode: string) {
    const { isAuthorized } = useAuthContext();
    return useQuery({
        queryKey: queryKeys.problemSets.invitedUsers(shareCode),
        enabled: isAuthorized,
        queryFn: ({ signal }) => getInvitedUsers(shareCode, signal),
    });
}

export function useProblemSetExpireInviteMutation() {
    return useMutation({
        mutationFn: ({ shareCode, username }: { shareCode: string; username: string }) =>
            expireInvite(shareCode, { inviteeName: username }),
        onSuccess: (_data, { shareCode }) =>
            void queryClient.invalidateQueries({ queryKey: queryKeys.problemSets.invitedUsers(shareCode) }),
    });
}
