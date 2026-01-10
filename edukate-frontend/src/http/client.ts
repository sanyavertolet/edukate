import axios from 'axios';

export const client = axios.create({
    baseURL: window.location.origin,
    withCredentials: true,
});
