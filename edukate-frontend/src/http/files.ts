import { useMutation } from "@tanstack/react-query";
import { client } from "./client";
import { defaultErrorHandler } from "./utils";

export function useDeleteTempFileMutation() {
    return useMutation({
        mutationKey: ['delete-temp-file'],
        mutationFn: async (fileName: string) => {
            try {
                const response = await client.delete<string>('/api/v1/files/temp', {
                    params: { key: fileName }
                });
                return response.data;
            } catch (error) {
                throw defaultErrorHandler(error);
            }
        }
    })
}

export function usePostTempFileMutation() {
    return useMutation({
        mutationKey: ['post-single-temp-file'],
        mutationFn: async ({ file, onProgress }: { file: File, onProgress?: (progress: number) => void }) => {
            try {
                const formData = new FormData();
                formData.append('content', file);
                const response = await client.post<string>('/api/v1/files/temp', formData, {
                    onUploadProgress: (progressEvent) => {
                        if (progressEvent.total && onProgress) {
                            const progress = Math.round((progressEvent.loaded * 100) / progressEvent.total);
                            onProgress(progress);
                        }
                    }
                });
                return response.data;
            } catch (error) {
                throw defaultErrorHandler(error);
            }
        }
    })
}
