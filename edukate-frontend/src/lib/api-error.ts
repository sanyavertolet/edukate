import { isAxiosError } from "axios";

export interface ApiError {
    timestamp: string;
    path: string;
    status: number;
    error: string;
    message?: string;
    requestId: string;
}

export function getApiErrorMessage(error: unknown): string {
    if (isAxiosError(error)) {
        const data = error.response?.data as Partial<ApiError> | undefined;
        return data?.message ?? data?.error ?? `${String(error.response?.status ?? "Unknown")} error`;
    }
    if (error instanceof Error) {
        return error.message;
    }
    return "Something went wrong";
}
