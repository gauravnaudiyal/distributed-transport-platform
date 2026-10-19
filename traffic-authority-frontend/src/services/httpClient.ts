import { useAuthStore } from '../store/auth';

const DEFAULT_API_BASE =
    import.meta.env.VITE_API_BASE_URL || import.meta.env.VITE_API_BASE || '/api';

export interface HttpRequestOptions extends RequestInit {
    baseUrl?: string;
    auth?: boolean;
    timeoutMs?: number;
}

export interface ApiErrorPayload {
    error?: string;
    message?: string;
    details?: string;
}

export class ApiError extends Error {
    status: number;

    constructor(status: number, message: string) {
        super(message);
        this.name = 'ApiError';
        this.status = status;
    }
}

function resolveMessage(payload: unknown, fallback: string): string {
    if (!payload || typeof payload !== 'object') {
        return fallback;
    }

    const typed = payload as ApiErrorPayload;
    return typed.error || typed.message || typed.details || fallback;
}

async function withTimeout<T>(promise: Promise<T>, timeoutMs: number): Promise<T> {
    let timeoutHandle: ReturnType<typeof setTimeout> | undefined;
    const timeoutPromise = new Promise<never>((_, reject) => {
        timeoutHandle = setTimeout(() => {
            reject(new Error(`Request timed out after ${timeoutMs}ms`));
        }, timeoutMs);
    });

    try {
        return await Promise.race([promise, timeoutPromise]);
    } finally {
        if (timeoutHandle) {
            clearTimeout(timeoutHandle);
        }
    }
}

async function parseBody(response: Response): Promise<unknown> {
    if (response.status === 204) {
        return null;
    }

    const contentType = response.headers.get('content-type') || '';
    if (!contentType.includes('application/json')) {
        return null;
    }
    try {
        return await response.json();
    } catch {
        return null;
    }
}

export async function request<T>(
    path: string,
    options: HttpRequestOptions = {}
): Promise<T> {
    const {
        baseUrl = DEFAULT_API_BASE,
        auth = true,
        timeoutMs = 10_000,
        headers,
        ...init
    } = options;

    const token = useAuthStore.getState().token;
    const authHeaders =
        auth && token ? ({ Authorization: `Bearer ${token}` } as const) : {};

    const response = await withTimeout(
        fetch(`${baseUrl}${path}`, {
            ...init,
            cache: 'no-store',
            headers: {
                'Content-Type': 'application/json',
                ...authHeaders,
                ...headers,
            },
        }),
        timeoutMs
    );

    const body = await parseBody(response);
    if (!response.ok) {
        const message = resolveMessage(
            body,
            `Request failed with status ${response.status}`
        );
        throw new ApiError(response.status, message);
    }

    return body as T;
}

export const httpClient = {
    get: <T>(path: string, options?: HttpRequestOptions) =>
        request<T>(path, { ...options, method: 'GET' }),
    post: <T>(path: string, body?: unknown, options?: HttpRequestOptions) =>
        request<T>(path, {
            ...options,
            method: 'POST',
            body: body === undefined ? undefined : JSON.stringify(body),
        }),
    put: <T>(path: string, body?: unknown, options?: HttpRequestOptions) =>
        request<T>(path, {
            ...options,
            method: 'PUT',
            body: body === undefined ? undefined : JSON.stringify(body),
        }),
    delete: <T>(path: string, options?: HttpRequestOptions) =>
        request<T>(path, { ...options, method: 'DELETE' }),
};
