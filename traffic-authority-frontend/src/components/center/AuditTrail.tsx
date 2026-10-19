import { useEffect, useMemo, useState } from 'react';
import type { AuditTrailEvent } from '../../types/index';
import { usePermitStore } from '../../store/permits';
import { useBookingStore } from '../../store/booking';
import { auditApiService } from '../../services/auditApi';
import type { AuditRecord } from '../../types/audit';
import { Button } from '../common/Button';

interface AuditTrailProps {
    events: AuditTrailEvent[];
    permitRoute?: string;
}

export function AuditTrail({ events, permitRoute = 'PERMIT-9912 · LDN → AMS' }: AuditTrailProps) {
    const { selectedPermitId } = usePermitStore();
    const { bookings } = useBookingStore();
    const [liveEvents, setLiveEvents] = useState<AuditTrailEvent[]>([]);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [lastUpdated, setLastUpdated] = useState<string | null>(null);

    const selectedBookingId = useMemo(() => {
        if (selectedPermitId) {
            const matchingBooking = bookings.find(
                (booking) => booking.id === selectedPermitId
            );
            if (matchingBooking) {
                return matchingBooking.id;
            }
        }
        const mostRecent = [...bookings].sort(
            (a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
        )[0];
        return mostRecent?.id;
    }, [selectedPermitId, bookings]);

    const selectedRouteLabel = useMemo(() => {
        const selected = bookings.find((booking) => booking.id === selectedBookingId);
        if (!selected) {
            return permitRoute;
        }
        return `${selected.id} · ${selected.origin} -> ${selected.destination}`;
    }, [bookings, selectedBookingId, permitRoute]);

    const loadAudit = async () => {
        if (!selectedBookingId) {
            setLiveEvents([]);
            setLastUpdated(new Date().toLocaleTimeString());
            return;
        }

        try {
            setIsLoading(true);
            setError(null);
            const records = await auditApiService.getBookingAudit(selectedBookingId);
            const mapped = records.map((record: AuditRecord) => ({
                id: record.id,
                message: record.eventType || 'Audit event',
                timestamp: record.createdAt
                    ? new Date(record.createdAt).toLocaleString()
                    : 'Unknown time',
                status: 'completed' as const,
            }));
            setLiveEvents(mapped);
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Failed to load audit trail');
            setLiveEvents([]);
        } finally {
            setLastUpdated(new Date().toLocaleTimeString());
            setIsLoading(false);
        }
    };

    useEffect(() => {
        void loadAudit();
    }, [selectedBookingId]);

    const displayedEvents = liveEvents.length > 0 ? liveEvents : events;

    return (
        <div className="bg-traffic-panel border border-traffic-border p-5">
            <div className="font-mono text-xs text-traffic-text-3 uppercase tracking-widest mb-4 flex items-center gap-2.5">
                <span>Consensus Audit Trail</span>
                <div className="flex-1 h-px bg-traffic-border" />
            </div>

            <div className="font-mono text-xs text-traffic-text-3 uppercase mb-4">
                {selectedRouteLabel}
            </div>

            <div className="flex items-center justify-between mb-3">
                <Button
                    size="sm"
                    variant="ghost"
                    onClick={() => void loadAudit()}
                    disabled={isLoading}
                >
                    {isLoading ? 'Refreshing...' : 'Refresh Audit'}
                </Button>
                <span className="font-mono text-xs text-traffic-text-3">
                    Last updated: {lastUpdated || 'never'}
                </span>
            </div>

            {isLoading && (
                <div className="font-mono text-xs text-traffic-text-3 mb-3">
                    Loading live audit events...
                </div>
            )}
            {error && (
                <div className="font-mono text-xs text-traffic-amber-2 mb-3">
                    {error} (showing fallback timeline)
                </div>
            )}

            <div className="space-y-3.5">
                {displayedEvents.map((event, index) => (
                    <div key={event.id} className="flex gap-3.5 relative">
                        {/* Timeline line */}
                        {index < displayedEvents.length - 1 && (
                            <div
                                className="absolute left-3 top-6 bottom-0 w-0.5 bg-traffic-border"
                            />
                        )}

                        {/* Dot */}
                        <div className="w-6 h-6 flex items-center justify-center flex-shrink-0 relative">
                            <div
                                className={`w-2 h-2 rounded-full ${event.status === 'completed'
                                        ? 'bg-traffic-green'
                                        : 'bg-traffic-amber-2 animate-amb-pulse'
                                    }`}
                                style={{
                                    boxShadow:
                                        event.status === 'completed'
                                            ? '0 0 6px rgb(0, 255, 136)'
                                            : '0 0 6px rgb(243, 156, 18)',
                                }}
                            />
                        </div>

                        {/* Content */}
                        <div className="flex-1">
                            <div className={`font-mono text-xs ${event.status === 'pending' ? 'text-traffic-amber-2' : 'text-traffic-text-2'
                                }`}>
                                {event.message}
                            </div>
                            <div className="font-mono text-xs text-traffic-text-3 mt-1">
                                {event.timestamp}
                            </div>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
}
