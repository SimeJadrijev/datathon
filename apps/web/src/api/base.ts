import axios from 'axios';
import type {ErrorResponseType} from "@/types";

export const api = axios.create({
    baseURL: '/api',
    headers: {
        'Content-Type': 'application/json',
    },
});

const UNAVAILABLE_MESSAGE = 'Poslužitelj trenutno nije dostupan. Pokušaj ponovno za nekoliko trenutaka.';

// Keeps the HTTP status so screens can show their own message for 404, 502 and so on.
export class ApiError extends Error {
    readonly status?: number;

    constructor(message: string, status?: number) {
        super(message);
        this.name = 'ApiError';
        this.status = status;
    }
}

api.interceptors.response.use(
    (response) => response.data,
    (error: ErrorResponseType) => {
        return Promise.reject(new ApiError(error.response?.data?.message || UNAVAILABLE_MESSAGE, error.response?.status));
    },
);
