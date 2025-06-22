import { AxiosResponse, isAxiosError } from "axios";

export function defaultErrorHandler(error: unknown) {
    if (isAxiosError(error)) {
        const status = fullStatus(error.response);
        return new Error(`Error fetching data: ${status}`);
    }
    return new Error(`An unknown error occurred. Please try again later.`);
}

const fullStatus = (response: AxiosResponse | undefined) => {
    const statusCode = response?.status
    const statusText = response?.statusText;
    if (statusText && statusCode) {
        return `${statusCode} ${statusText}`;
    } else if (statusCode) {
        return `${statusCode}`;
    } else {
        return "Unknown Status";
    }
};
