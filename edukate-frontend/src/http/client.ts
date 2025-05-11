import axios from 'axios';
import { getTokenFromCookies, removeCookies } from "../utils/cookies";
import { showNotification, NotificationOptions } from "../components/snackbars/NotificationContextProvider";
import { queryClient } from './queryClient';

/**
 * Axios client instance configured with base URL
 */
export const client = axios.create({
    baseURL: window.location.origin,
});

/**
 * Handles 401 Unauthorized errors by:
 * 1. Removing authentication cookies
 * 2. Invalidating user-related queries
 * 3. Showing a user-friendly error message
 */
const handle401Error = () => {
    removeCookies();
    queryClient.invalidateQueries({ queryKey: ['whoami'] }).finally();

    // Show a user-friendly error message with custom styling
    const notificationOptions: NotificationOptions = {
        severity: "warning",
        variant: "filled",
        autoHideDuration: 6000
    };

    showNotification("Oops! Your session has timed out. Please sign in again.", notificationOptions);
};

client.interceptors.request.use(
    (config) => {
        const token = getTokenFromCookies();
        if (token && config.headers) {
            config.headers['Authorization'] = `Bearer ${token}`;
        }
        return config;
    },
    (error) => Promise.reject(error),
);

client.interceptors.response.use(
    (response) => { return response; },
    (error) => {
        if (error.response && error.response.status === 401) {
            handle401Error();
        }
        return Promise.reject(error);
    },
);
