import { useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { notificationApiService, type NotificationLog } from '../../services/notificationApi';
import { Button } from '../common/Button';

export function NotificationTimeline() {
    const { userId, username } = useAuth();
    const [notifications, setNotifications] = useState<NotificationLog[]>([]);
    const [error, setError] = useState<string | null>(null);

    const resolvedUserId = userId || username || 'civilian-demo';

    const loadNotifications = async () => {
        try {
            setError(null);
            const response = await notificationApiService.getNotifications(resolvedUserId);
            setNotifications(response);
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Failed to load notifications');
        }
    };

    return (
        <div className="bg-traffic-panel border border-traffic-border p-6">
            <div className="flex items-center justify-between mb-4">
                <h2 className="font-barlow font-bold text-lg uppercase tracking-wider text-traffic-white">
                    Notification Log
                </h2>
                <Button onClick={loadNotifications}>Load Notifications</Button>
            </div>
            {error && <p className="text-xs text-traffic-red font-mono mb-3">{error}</p>}
            {notifications.length === 0 ? (
                <p className="text-xs text-traffic-text-2 font-mono">
                    No notifications loaded yet.
                </p>
            ) : (
                <div className="space-y-2">
                    {notifications.map((item) => (
                        <div
                            key={item.id}
                            className="bg-traffic-bg border border-traffic-border p-3"
                        >
                            <p className="font-mono text-xs text-traffic-accent">
                                {item.status} / Booking {item.bookingId}
                            </p>
                            <p className="font-mono text-xs text-traffic-text-2 mt-1">
                                {item.message}
                            </p>
                            <p className="font-mono text-xs text-traffic-text-3 mt-1">
                                {new Date(item.sentAt).toLocaleString()}
                            </p>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}
