import axios from 'axios';
import { getTokenFromCookies, removeCookies } from "../utils/cookies";
import { queryClient } from './queryClient';
import { toast } from "react-toastify";

export const client = axios.create({
    baseURL: window.location.origin,
});

const handle401Error = () => {
    removeCookies();
    queryClient.invalidateQueries({ queryKey: ['whoami'] }).finally();
    toast.warn("Oops! Your session has timed out. Please sign in again.");
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
