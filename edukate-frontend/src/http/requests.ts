import { useMutation, useQuery } from "@tanstack/react-query";
import { client } from "./client";
import { useAuthContext } from "../components/auth/AuthContextProvider";
import { CreateBundleRequest } from "../types/bundle/CreateBundleRequest";
import { Bundle, BundleCategory } from "../types/bundle/Bundle";
import { BundleMetadata } from "../types/bundle/BundleMetadata";
import { BaseNotification } from "../types/notification/BaseNotification";
import { Problem } from "../types/problem/Problem";
import { ProblemMetadata } from "../types/problem/ProblemMetadata";
import { Result } from "../types/problem/Result";
import { CreateSubmissionRequest } from "../types/submission/CreateSubmissionRequest";
import { Submission } from "../types/submission/Submission";
import { UserWithRole } from "../types/user/UserWithRole";
import { defaultErrorHandler } from "./utils";
import { NotificationStatistics } from "../types/notification/NotificationStatistics";

export function useProblemListRequest(page: number, size: number) {
    const { user } = useAuthContext();
    return useQuery({
        queryKey: ['problemList', page, size, user],
        queryFn: async () => {
            try {
                const response = await client.get(`/api/v1/problems?page=${page}&size=${size}`);
                return response.data as ProblemMetadata[];
            } catch (error) {
                throw defaultErrorHandler(error);
            }
        },
    });
}

export function useProblemCountRequest() {
    return useQuery({
        queryKey: ['problemCount'],
        queryFn: async () => {
            try {
                const response = await client.get<number>('/api/v1/problems/count');
                return response.data;
            } catch (error) {
                throw defaultErrorHandler(error);
            }
        },
    });
}

export function useProblemRequest(id: string | undefined, shouldRefresh?: boolean) {
    const { user } = useAuthContext();
    return useQuery({
        queryKey: ['problem', id, user, shouldRefresh],
        queryFn: async () => {
            if (id === undefined) {
                return undefined;
            }
            try {
                const response = await client.get<Problem>(`/api/v1/problems/${id}`);
                return response.data;
            } catch (error) {
                throw defaultErrorHandler(error);
            }
        },
    });
}

export function useSubmitProblemMutation() {
    const { user } = useAuthContext();
    return useMutation({
        mutationKey: ['submission', user],
        mutationFn: async (request: CreateSubmissionRequest) => {
            if (!user) {
                throw new Error("User not signed in");
            }
            try {
                const response = await client.post<Submission>('/api/v1/submissions', request);
                return response.data;
            } catch (error) {
                throw defaultErrorHandler(error);
            }
        },
    });
}

// todo: remove me
export function useMySubmissionsRequest(problemId: string) {
    const { user } = useAuthContext();
    return useQuery({
        queryKey: ['submission', problemId, user?.name],
        queryFn: async () => {
            if (!user) {
                throw new Error("User not signed in");
            }
            try {
                const response = await client.get<Submission[]>(`/api/v1/submissions/${problemId}/${user.name}`);
                return response.data;
            } catch (error) {
                throw defaultErrorHandler(error);
            }
        },
    });
}

export function useResultRequest(problemId: string) {
    return useQuery({
        queryKey: ['result', problemId],
        queryFn: async () => {
            try {
                const response = await client.get<Result>(`/api/v1/results/${problemId}`);
                return response.data;
            } catch (error) {
                throw defaultErrorHandler(error);
            }
        }
    })
}

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
                const response = await client.post<BundleMetadata>(`/api/v1/bundles/join/${bundleCode}`);
                return response.data;
            } catch (error) {
                throw defaultErrorHandler(error);
            }
        }
    })
}

export function useOptionsRequest<T = string>(urlPath: string, prefix: string, size: number = 5) {
    return useQuery({
        queryKey: [urlPath, prefix, size],
        queryFn: async () => {
            try {
                const response = await client.get<T[]>(urlPath, {
                    params: { prefix: prefix, size: size, }
                });
                return response.data;
            } catch (error) {
                throw defaultErrorHandler(error);
            }
        }
    })
}

export function useNotificationsCountRequest(isRead?: boolean) {
    const { isAuthorized } = useAuthContext();
    return useQuery({
        queryKey: ['notifications', 'count', isRead, isAuthorized],
        queryFn: async () => {
            if (!isAuthorized) {
                return null;
            }
            try {
                const response = await client.get<NotificationStatistics>('/api/v1/notifications/count', {
                    params: { isRead }
                });
                return response.data;
            } catch (error) {
                throw defaultErrorHandler(error);
            }
        }
    })
}

export function useGetNotificationsRequest(isRead?: boolean, size: number = 10, page: number = 0) {
    const { isAuthorized } = useAuthContext();
    return useQuery({
        queryKey: ['notifications', 'get', isRead, size, page, isAuthorized],
        queryFn: async () => {
            if (!isAuthorized) {
                return null;
            }
            try {
                const response = await client.get<BaseNotification[]>('/api/v1/notifications', {
                    params: {isRead, size, page}
                });
                return response.data;
            } catch (error) {
                throw defaultErrorHandler(error);
            }
        }
    })
}

export function useMarkNotificationsAsReadMutation() {
    const { isAuthorized } = useAuthContext();
    return useMutation({
        mutationKey: ['notifications', 'mark-as-read'],
        mutationFn: async (uuids: string[]) => {
            if (!isAuthorized) {
                return null;
            }
            try {
                const response = await client.post('/api/v1/notifications/mark-as-read', uuids);
                return response.data as number;
            } catch (error) {
                throw defaultErrorHandler(error);
            }
        }
    })
}

export function useMarkAllNotificationsAsReadMutation() {
    const { isAuthorized } = useAuthContext();
    return useMutation({
        mutationKey: ['notifications', 'mark-all-as-read'],
        mutationFn: async () => {
            if (!isAuthorized) {
                return null;
            }
            try {
                const response = await client.post('/api/v1/notifications/mark-all-as-read');
                return response.data as number;
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
                    params: { shareCode, inviteeId: username }
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
        mutationFn: async ({shareCode, isAccepted}: { shareCode: string, isAccepted: boolean }) => {
            if (!isAuthorized) {
                return null;
            }
            try {
                const response = await client.post<string>(`/api/v1/bundles/${shareCode}/reply-invite`, undefined, {
                    params: { shareCode, response: isAccepted }
                });
                return response.status;
            } catch (error) {
                throw defaultErrorHandler(error);
            }
        }
    })
}

export function useBundleChangeUserRoleMutation() {
    const { isAuthorized } = useAuthContext();
    return useMutation({
        mutationKey: ['bundle', 'user', 'change-role'],
        mutationFn: async ({username, role, shareCode} : {username: string, role: string, shareCode: string}) => {
            if (!isAuthorized) {
                return null;
            }
            try {
                const response = await client.post<string>(`/api/v1/bundles/${shareCode}/change-role`, undefined, {
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
                const response = await client.get<UserWithRole[]>(`/api/v1/bundles/${shareCode}/users`);
                return response.data;
            } catch (error) {
                throw defaultErrorHandler(error);
            }
        }
    })
}
