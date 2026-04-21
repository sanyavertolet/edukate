import { isAxiosError } from "axios";

export interface ApiError {
    timestamp: string;
    path: string;
    status: number;
    error: string;
    message?: string;
    requestId: string;
}

const STATUS_MESSAGES: Record<number, string> = {
    400: "Bad request",
    401: "Authentication required",
    403: "Access denied",
    404: "Not found",
    409: "Conflict",
    422: "Invalid input",
    500: "Server error",
    502: "Service unavailable",
    503: "Service unavailable",
};

const MAX_MESSAGE_LENGTH = 80;

export function getApiErrorMessage(error: unknown): string {
    if (isAxiosError(error)) {
        const data = error.response?.data as Partial<ApiError> | undefined;
        const status = error.response?.status;
        const serverMessage = data?.message;
        if (serverMessage && serverMessage.length <= MAX_MESSAGE_LENGTH) {
            return serverMessage;
        }
        if (status === undefined) return "Something went wrong";
        return STATUS_MESSAGES[status] ?? `Error ${String(status)}`;
    }
    if (error instanceof Error && error.message.length <= MAX_MESSAGE_LENGTH) {
        return error.message;
    }
    return "Something went wrong";
}
