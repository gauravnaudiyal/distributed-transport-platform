import { httpClient } from './httpClient';

const API_BASE =
    import.meta.env.VITE_API_BASE_URL || import.meta.env.VITE_API_BASE || '/api';

const NOTIFICATION_BASE =
    import.meta.env.VITE_NOTIFICATION_API_BASE_URL ||
    API_BASE ||
    import.meta.env.VITE_GATEWAY_BASE_URL ||
    '/api';

export interface NotificationLog {
    id: number;
    userId: string;
    bookingId: string;
    status: string;
    message: string;
    sentAt: string;
}

export const notificationApiService = {
    async getNotifications(userId: string): Promise<NotificationLog[]> {
        const response = await httpClient.get<NotificationLog[] | null>(
            `/notifications/${encodeURIComponent(userId)}`,
            {
                baseUrl: NOTIFICATION_BASE,
            }
        );
        return response || [];
    },
};
