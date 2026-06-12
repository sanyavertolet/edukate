import { useMemo } from "react";
import { useMutation, useQuery } from "@tanstack/react-query";
import {
    acceptInvitation,
    createInvitation,
    createProblemSet,
    declineInvitation,
    getInvitations,
    getMembers,
    getMemberProblemSets,
    GetMemberProblemSetsRolesItem,
    getProblemSetByShareCode,
    getPublicProblemSets,
    leaveProblemSet,
    removeMember,
    revokeInvitation,
    searchMemberProblemSets,
    setMemberRole,
    SetMemberRoleRequestRole,
    updateSettings,
    UpdateProblemSetSettingsRequest,
} from "@/generated/backend";
import { useAuthContext } from "@/features/auth/context";
import { queryClient } from "@/lib/query-client";
import { queryKeys } from "@/lib/query-keys";
import { ProblemSetCategory, CreateProblemSetRequest, ProblemSetMetadata } from "./types";

function invalidateProblemSet(shareCode: string) {
    void queryClient.invalidateQueries({ queryKey: queryKeys.problemSets.detail(shareCode) });
    void queryClient.invalidateQueries({ queryKey: queryKeys.problemSets.users(shareCode) });
    void queryClient.invalidateQueries({ queryKey: queryKeys.problemSets.invitedUsers(shareCode) });
    void queryClient.invalidateQueries({ queryKey: queryKeys.problemSets.all });
}

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
        onSuccess: () => void queryClient.invalidateQueries({ queryKey: queryKeys.problemSets.list("admin") }),
    });
}

export function useProblemSetRequest(problemSetCode: string | undefined) {
    return useQuery({
        queryKey: queryKeys.problemSets.detail(problemSetCode ?? ""),
        queryFn: ({ signal }) => getProblemSetByShareCode(problemSetCode as string, signal),
        enabled: !!problemSetCode,
    });
}

// String-literal map (typed against the generated union) — using the const-object
// like GetMemberProblemSetsRolesItem.USER trips some TS-language-service versions on
// Orval's self-referential `type X = typeof X[keyof typeof X]` pattern.
const ROLES_BY_CATEGORY: Record<"user" | "moderator" | "admin", GetMemberProblemSetsRolesItem[]> = {
    user: ["USER"],
    moderator: ["MODERATOR"],
    admin: ["ADMIN"],
};

export function useProblemSetsRequest(category: ProblemSetCategory) {
    return useQuery<ProblemSetMetadata[]>({
        queryKey: queryKeys.problemSets.list(category),
        queryFn: ({ signal }) =>
            category === "public"
                ? getPublicProblemSets(undefined, signal)
                : getMemberProblemSets({ roles: ROLES_BY_CATEGORY[category] }, signal),
    });
}

export function useMemberProblemSetsSearch(problemKey?: string) {
    const { isAuthorized } = useAuthContext();
    const query = useQuery<ProblemSetMetadata[]>({
        queryKey: queryKeys.problemSets.search(problemKey),
        enabled: isAuthorized,
        queryFn: ({ signal }) => {
            return searchMemberProblemSets({ page: 0, size: 200, problemKey }, signal);
        },
    });
    const data = useMemo<ProblemSetMetadata[]>(
        () => [...(query.data ?? [])].sort((a, b) => a.name.localeCompare(b.name)),
        [query.data],
    );
    return { data, isLoading: query.isLoading };
}

export function useProblemSetUserListQuery(shareCode: string) {
    const { isAuthorized } = useAuthContext();
    return useQuery({
        queryKey: queryKeys.problemSets.users(shareCode),
        enabled: isAuthorized,
        queryFn: ({ signal }) => getMembers(shareCode, signal),
    });
}

export function useProblemSetInvitedUserListQuery(shareCode: string) {
    const { isAuthorized } = useAuthContext();
    return useQuery({
        queryKey: queryKeys.problemSets.invitedUsers(shareCode),
        enabled: isAuthorized,
        queryFn: ({ signal }) => getInvitations(shareCode, signal),
    });
}

export function useProblemSetUpdateSettingsMutation() {
    return useMutation({
        mutationFn: ({ shareCode, data }: { shareCode: string; data: UpdateProblemSetSettingsRequest }) =>
            updateSettings(shareCode, data),
        onSuccess: (_data, { shareCode }) => {
            invalidateProblemSet(shareCode);
        },
    });
}

export function useProblemSetSetMemberRoleMutation() {
    return useMutation({
        mutationFn: ({
            shareCode,
            username,
            role,
        }: {
            shareCode: string;
            username: string;
            role: SetMemberRoleRequestRole;
        }) => setMemberRole(shareCode, username, { role }),
        onSuccess: (_data, { shareCode }) => {
            invalidateProblemSet(shareCode);
        },
    });
}

export function useProblemSetRemoveMemberMutation() {
    return useMutation({
        mutationFn: ({ shareCode, username }: { shareCode: string; username: string }) => removeMember(shareCode, username),
        onSuccess: (_data, { shareCode }) => {
            invalidateProblemSet(shareCode);
        },
    });
}

export function useProblemSetLeaveMutation() {
    return useMutation({
        mutationFn: ({ shareCode }: { shareCode: string }) => leaveProblemSet(shareCode),
        onSuccess: (_data, { shareCode }) => {
            invalidateProblemSet(shareCode);
        },
    });
}

export function useProblemSetCreateInvitationMutation() {
    return useMutation({
        mutationFn: ({ shareCode, inviteeName }: { shareCode: string; inviteeName: string }) =>
            createInvitation(shareCode, { inviteeName }),
        onSuccess: (_data, { shareCode }) => {
            invalidateProblemSet(shareCode);
            void queryClient.invalidateQueries({ queryKey: queryKeys.users.byPrefix });
        },
    });
}

export function useProblemSetRevokeInvitationMutation() {
    return useMutation({
        mutationFn: ({ shareCode, username }: { shareCode: string; username: string }) =>
            revokeInvitation(shareCode, username),
        onSuccess: (_data, { shareCode }) => {
            invalidateProblemSet(shareCode);
            void queryClient.invalidateQueries({ queryKey: queryKeys.users.byPrefix });
        },
    });
}

export function useProblemSetAcceptInvitationMutation() {
    return useMutation({
        mutationFn: ({ shareCode }: { shareCode: string }) => acceptInvitation(shareCode),
        onSuccess: (_data, { shareCode }) => {
            invalidateProblemSet(shareCode);
        },
    });
}

export function useProblemSetDeclineInvitationMutation() {
    return useMutation({
        mutationFn: ({ shareCode }: { shareCode: string }) => declineInvitation(shareCode),
        onSuccess: (_data, { shareCode }) => {
            invalidateProblemSet(shareCode);
        },
    });
}
