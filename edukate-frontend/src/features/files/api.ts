import { useMutation, useQuery } from "@tanstack/react-query";
import { axiosInstance as client } from "@/lib/axios";
import { deleteTempFile, getTempFiles } from "@/generated/backend";
import { queryKeys } from "@/lib/query-keys";

export function useDeleteTempFileMutation() {
    return useMutation({
        mutationFn: (fileName: string) => deleteTempFile({ fileName }),
    });
}

// Kept manual: FormData body + onUploadProgress, not representable in the generated client
export function usePostTempFileMutation() {
    return useMutation({
        mutationFn: async ({ file, onProgress }: { file: File; onProgress?: (progress: number) => void }) => {
            const formData = new FormData();
            formData.append("content", file);
            const response = await client.post<string>("/api/v1/files/temp", formData, {
                timeout: 0, // no cap — upload duration depends on file size and connection speed
                onUploadProgress: (progressEvent) => {
                    if (progressEvent.total && onProgress) {
                        const progress = Math.round((progressEvent.loaded * 100) / progressEvent.total);
                        onProgress(progress);
                    }
                },
            });
            return response.data;
        },
    });
}

export function useGetTempFiles() {
    return useQuery({
        queryKey: queryKeys.files.temp,
        queryFn: ({ signal }) => getTempFiles(signal),
    });
}

// Kept manual: explicit blob responseType, not inferable by Orval from spec
export function useGetTempFile(fileName: string | undefined) {
    return useQuery({
        queryKey: queryKeys.files.tempFile(fileName ?? ""),
        queryFn: async () => {
            const response = await client.get<Blob>(`/api/v1/files/temp/get`, {
                responseType: "blob",
                params: { fileName },
            });
            return response.data;
        },
        enabled: !!fileName,
    });
}
