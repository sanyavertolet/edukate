import axios from 'axios';
import { getTokenFromCookies, removeCookies } from "../utils/cookies";

export const client = axios.create({
    baseURL: window.location.origin,
});

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
    (config) => {
        if (config.status == 401) {
            removeCookies();
        }
        return config;
    },
    (error) => Promise.reject(error),
);
