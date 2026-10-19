import { httpClient } from './httpClient';

const API_BASE =
    import.meta.env.VITE_API_BASE_URL || import.meta.env.VITE_API_BASE || '/api';

const USER_BASE =
    import.meta.env.VITE_USER_API_BASE_URL ||
    API_BASE ||
    import.meta.env.VITE_GATEWAY_BASE_URL ||
    '/api';

export interface UserProfile {
    id?: number;
    userId: string;
    fullName: string;
    email: string;
}

export interface UserPreference {
    userId: string;
    emailEnabled: boolean;
}

export const userApiService = {
    createUser(payload: UserProfile): Promise<UserProfile> {
        return httpClient.post<UserProfile>('/users', payload, {
            baseUrl: USER_BASE,
        });
    },

    getUser(userId: string): Promise<UserProfile> {
        return httpClient.get<UserProfile>(`/users/${encodeURIComponent(userId)}`, {
            baseUrl: USER_BASE,
        });
    },

    getPreferences(userId: string): Promise<UserPreference> {
        return httpClient.get<UserPreference>(
            `/users/${encodeURIComponent(userId)}/preferences`,
            {
                baseUrl: USER_BASE,
            }
        );
    },

    updatePreferences(
        userId: string,
        payload: Pick<UserPreference, 'emailEnabled'>
    ): Promise<UserPreference> {
        return httpClient.put<UserPreference>(
            `/users/${encodeURIComponent(userId)}/preferences`,
            payload,
            {
                baseUrl: USER_BASE,
            }
        );
    },
};
