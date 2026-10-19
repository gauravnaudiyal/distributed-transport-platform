import type { PermitItem } from '../../types/index';
import { PermitCard } from '../common/PermitCard';
import { Button } from '../common/Button';
import { useMemo, useState } from 'react';
import { useBookingStore } from '../../store/booking';
import { bookingApiService } from '../../services/bookingApi';
import { usePermitStore } from '../../store/permits';

interface ActivePermitsProps {
    permits: PermitItem[];
    onPermitClick?: (id: string) => void;
}

export function ActivePermits({ permits, onPermitClick }: ActivePermitsProps) {
    const [showAll, setShowAll] = useState(false);
    const [isCancelling, setIsCancelling] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const { selectedPermitId, setSelectedPermitId } = usePermitStore();
    const {
        bookings,
        cancelBooking,
        setCurrentBooking,
        setError: setBookingError,
        setLoading,
    } = useBookingStore();

    const bookingPermits = useMemo<PermitItem[]>(() => {
        return [...bookings]
            .sort(
                (a, b) =>
                    new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
            )
            .map((booking) => ({
                id: booking.id,
                route: `${booking.origin} -> ${booking.destination}`,
                origin: booking.origin,
                destination: booking.destination,
                status:
                    booking.state === 'CONFIRMED'
                        ? 'approved'
                        : booking.state === 'PENDING' || booking.state === 'RESERVED'
                            ? 'pending'
                            : 'denied',
                departureTime: new Date(booking.departureTime).toLocaleString(),
                eta: new Date(booking.arrivalTime).toLocaleTimeString(),
                vehicleType: 'CIVILIAN',
                issuedAt: new Date(booking.createdAt).toLocaleString(),
                reason:
                    booking.state === 'CANCELLED'
                        ? 'Booking cancelled'
                        : booking.state === 'REJECTED'
                            ? 'Rejected by booking service'
                            : undefined,
            }));
    }, [bookings]);

    const recentPermits = useMemo(
        () => bookingPermits.slice(0, 3),
        [bookingPermits]
    );
    const allPermits = useMemo(() => bookingPermits, [bookingPermits]);

    const displayedTop = bookingPermits.length > 0 ? recentPermits : permits.slice(0, 3);

    const handleCancelSelected = async () => {
        if (!selectedPermitId) {
            setError('Select a booking first.');
            return;
        }

        try {
            setIsCancelling(true);
            setError(null);
            setLoading(true);
            const updated = await bookingApiService.cancelBooking(selectedPermitId);
            cancelBooking(selectedPermitId);
            setCurrentBooking(updated);
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Cancellation failed';
            setError(message);
            setBookingError(message);
        } finally {
            setLoading(false);
            setIsCancelling(false);
        }
    };

    return (
        <div className="bg-traffic-panel border border-traffic-border p-5">
            <div className="font-mono text-xs text-traffic-text-3 uppercase tracking-widest mb-4 flex items-center gap-2.5">
                <span>Active Permits</span>
                <div className="flex-1 h-px bg-traffic-border" />
            </div>

            <div className="space-y-2">
                {displayedTop.map((permit) => (
                    <PermitCard
                        key={permit.id}
                        permit={permit}
                        onClick={() => {
                            setSelectedPermitId(permit.id);
                            onPermitClick?.(permit.id);
                        }}
                    />
                ))}
            </div>

            <div className="flex gap-2 mt-3.5">
                <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => setShowAll((current) => !current)}
                >
                    {showAll ? 'Hide All Permits' : 'View All Permits'}
                </Button>
                <Button
                    variant="danger"
                    size="sm"
                    onClick={handleCancelSelected}
                    disabled={isCancelling || !selectedPermitId}
                >
                    {isCancelling ? 'Cancelling...' : 'Cancel Selected'}
                </Button>
            </div>

            {error && (
                <p className="font-mono text-xs text-traffic-red mt-3">{error}</p>
            )}

            {showAll && (
                <div className="mt-4 pt-4 border-t border-traffic-border">
                    <div className="font-mono text-xs text-traffic-text-3 uppercase tracking-widest mb-3">
                        All Bookings
                    </div>
                    <div className="space-y-2 max-h-80 overflow-y-auto">
                        {(bookingPermits.length > 0 ? allPermits : permits).map((permit) => (
                            <PermitCard
                                key={`all-${permit.id}`}
                                permit={permit}
                                onClick={() => {
                                    setSelectedPermitId(permit.id);
                                    onPermitClick?.(permit.id);
                                }}
                            />
                        ))}
                    </div>
                </div>
            )}
        </div>
    );
}
