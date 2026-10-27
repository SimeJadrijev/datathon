import axios from 'axios';
import type {ErrorResponseType} from "@/types";

export const api = axios.create({
    baseURL: '/api',
    headers: {
        'Content-Type': 'application/json',
    },
});

const UNAVAILABLE_MESSAGE = 'Poslužitelj trenutno nije dostupan. Pokušaj ponovno za nekoliko trenutaka.';

api.interceptors.response.use(
    (response) => response.data,
    (error: ErrorResponseType) => {
        return Promise.reject(new Error(error.response?.data?.message || UNAVAILABLE_MESSAGE));
    },
);
