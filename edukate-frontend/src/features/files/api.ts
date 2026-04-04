import { useMutation, useQuery } from "@tanstack/react-query";
import { client } from "@/lib/axios";
import { defaultErrorHandler } from "@/lib/error-handler";
import { queryKeys } from "@/lib/query-keys";
import { FileMetadata } from "./types";

export function useDeleteTempFileMutation() {
    return useMutation({
        mutationKey: ["delete-temp-file"],
        mutationFn: async (fileName: string) => {
            try {
                const response = await client.delete<string>("/api/v1/files/temp", {
                    params: { fileName },
                });
                return response.data;
            } catch (error) {
                throw defaultErrorHandler(error);
            }
        },
    });
}

export function usePostTempFileMutation() {
    return useMutation({
        mutationKey: ["post-single-temp-file"],
        mutationFn: async ({ file, onProgress }: { file: File; onProgress?: (progress: number) => void }) => {
            try {
                const formData = new FormData();
                formData.append("content", file);
                const response = await client.post<string>("/api/v1/files/temp", formData, {
                    onUploadProgress: (progressEvent) => {
                        if (progressEvent.total && onProgress) {
                            const progress = Math.round((progressEvent.loaded * 100) / progressEvent.total);
                            onProgress(progress);
                        }
                    },
                });
                return response.data;
            } catch (error) {
                throw defaultErrorHandler(error);
            }
        },
    });
}

export function useGetTempFiles() {
    return useQuery({
        queryKey: queryKeys.files.temp,
        queryFn: async () => {
            try {
                const response = await client.get<FileMetadata[]>("/api/v1/files/temp");
                return response.data;
            } catch (error) {
                throw defaultErrorHandler(error);
            }
        },
    });
}

export function useGetTempFile(fileName: string | undefined) {
    return useQuery({
        queryKey: queryKeys.files.tempFile(fileName ?? ""),
        queryFn: async () => {
            if (!fileName) {
                return undefined;
            }
            try {
                const response = await client.get<Blob>(`/api/v1/files/temp/get`, {
                    responseType: "blob",
                    params: { fileName },
                });
                return response.data;
            } catch (error) {
                throw defaultErrorHandler(error);
            }
        },
        enabled: !!fileName,
    });
}
