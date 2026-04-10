import axios, { AxiosError } from "axios";
import type { AxiosRequestConfig, AxiosResponse } from "axios";
import { queryClient } from "./query-client";
import { queryKeys } from "./query-keys";

export const axiosInstance = axios.create({
    baseURL: window.location.origin,
    withCredentials: true,
    timeout: 30_000,
});

// On 401 invalidate whoami — auth context reacts and hides protected UI naturally,
// without a hard redirect
axiosInstance.interceptors.response.use(
    (response) => response,
    (error: AxiosError) => {
        if (error.response?.status === 401) {
            void queryClient.invalidateQueries({ queryKey: queryKeys.auth.whoami });
        }
        return Promise.reject(error);
    },
);

// Orval custom mutator — wraps the shared axios instance, unwrapping to data directly.
// Orval infers the return type as T; returning AxiosResponse<T> would propagate the
// wrapper into all generated types.
export const client = <T = unknown>(config: AxiosRequestConfig): Promise<T> =>
    axiosInstance(config).then((response: AxiosResponse<T>) => response.data);
