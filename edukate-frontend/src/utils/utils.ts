import { AxiosResponse } from "axios";

export const fullStatus = (response: AxiosResponse | undefined) => {
    const statusCode = response?.status
    const statusText = response?.statusText;
    if (statusText && statusCode) {
        return `${statusCode} ${statusText}`;
    } else if (statusCode) {
        return `${statusCode}`;
    } else {
        return "Unknown Status"
    }
};
