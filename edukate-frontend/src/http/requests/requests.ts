import { useQuery } from "@tanstack/react-query";
import { client } from "../client";
import { defaultErrorHandler } from "../utils";

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

