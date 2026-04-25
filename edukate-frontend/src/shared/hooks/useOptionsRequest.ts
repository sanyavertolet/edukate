import { useQuery } from "@tanstack/react-query";
import { axiosInstance as client } from "@/lib/axios";

export function useOptionsRequest<T = string>(
    urlPath: string,
    prefix: string,
    size: number = 5,
    extraParams?: Record<string, string>,
) {
    return useQuery({
        queryKey: [urlPath, prefix, size, extraParams],
        queryFn: async () => {
            const response = await client.get<T[]>(urlPath, { params: { prefix, size, ...extraParams } });
            return response.data;
        },
    });
}
