import { httpClient } from './httpClient';
import type {
    CompatibilityRequest,
    CompatibilityResponse,
} from '../types/compatibility';

const API_BASE =
    import.meta.env.VITE_API_BASE_URL || import.meta.env.VITE_API_BASE || '/api';

const COMPATIBILITY_BASE =
    import.meta.env.VITE_COMPATIBILITY_API_BASE_URL ||
    API_BASE ||
    import.meta.env.VITE_GATEWAY_BASE_URL ||
    '/api';

export const compatibilityApiService = {
    async checkCompatibility(
        payload: CompatibilityRequest
    ): Promise<CompatibilityResponse> {
        return httpClient.post<CompatibilityResponse>(
            '/compatibility/check-compatibility',
            payload,
            { baseUrl: COMPATIBILITY_BASE }
        );
    },
};
