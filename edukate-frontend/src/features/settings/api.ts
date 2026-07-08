import { useMutation } from "@tanstack/react-query";
import { axiosInstance as client } from "@/lib/axios";
import { deleteAvatar, requestEmailChange } from "@/generated/backend";
import { changePassword, changeUsername } from "@/generated/gateway";
import { queryClient } from "@/lib/query-client";
import { queryKeys } from "@/lib/query-keys";

// Kept manual: FormData body (multipart/form-data with field "content") —
// the generated client emits a Blob signature that doesn't match our endpoint.
export function useUploadAvatarMutation() {
    return useMutation({
        mutationFn: async (file: Blob) => {
            const formData = new FormData();
            formData.append("content", file);
            const response = await client.put<string>("/api/v1/users/me/avatar", formData, { timeout: 0 });
            return response.data;
        },
        onSuccess: () => {
            void queryClient.invalidateQueries({ queryKey: queryKeys.auth.whoami });
        },
    });
}

export function useDeleteAvatarMutation() {
    return useMutation({
        mutationFn: () => deleteAvatar(),
        onSuccess: () => {
            void queryClient.invalidateQueries({ queryKey: queryKeys.auth.whoami });
        },
    });
}

export function useChangeUsernameMutation() {
    return useMutation({
        mutationFn: (newUsername: string) => changeUsername({ newUsername }),
        onSuccess: () => {
            void queryClient.invalidateQueries({ queryKey: queryKeys.auth.whoami });
        },
    });
}

export function useChangePasswordMutation() {
    return useMutation({
        mutationFn: ({ currentPassword, newPassword }: { currentPassword: string; newPassword: string }) =>
            changePassword({ currentPassword, newPassword }),
    });
}

export function useRequestEmailChangeMutation() {
    return useMutation({
        mutationFn: (newEmail: string) => requestEmailChange({ newEmail }),
    });
}
