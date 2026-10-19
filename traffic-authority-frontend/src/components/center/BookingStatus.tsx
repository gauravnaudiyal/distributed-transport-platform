import { useState } from 'react';
import type { Booking, RouteSegment } from '../../store/booking';
import { useBookingStore } from '../../store/booking';
import { bookingApiService } from '../../services/bookingApi';
import { auditApiService } from '../../services/auditApi';
import type { AuditRecord } from '../../types/audit';
import { Button } from '../common/Button';

interface BookingStatusProps {
    booking: Booking;
    onCancel?: () => void;
}

export function BookingStatus({ booking, onCancel }: BookingStatusProps) {
    const { cancelBooking, setLoading, setError, setCurrentBooking } = useBookingStore();
    const [verifyState, setVerifyState] = useState<string | null>(null);
    const [auditRecords, setAuditRecords] = useState<AuditRecord[]>([]);

    const stateColors: Record<string, string> = {
        PENDING: 'text-traffic-amber border-traffic-amber',
        RESERVED: 'text-traffic-accent border-traffic-accent',
        CONFIRMED: 'text-traffic-green border-traffic-green',
        REJECTED: 'text-traffic-red border-traffic-red',
        CANCELLED: 'text-traffic-text-2 border-traffic-text-2',
        FAILED: 'text-traffic-red border-traffic-red',
    };

    const stateDescription: Record<string, string> = {
        PENDING: 'Waiting for regional confirmation',
        RESERVED: 'Reserved across all segments',
        CONFIRMED: 'Booking confirmed and ready',
        REJECTED: 'Insufficient capacity on segments',
        CANCELLED: 'Booking cancelled',
        FAILED: 'Booking failed due to error',
    };

    const handleCancel = async () => {
        if (
            !window.confirm('Cancel this booking? Capacity will be released.')
        ) {
            return;
        }

        try {
            setLoading(true);
            await bookingApiService.cancelBooking(booking.id);
            cancelBooking(booking.id);
            onCancel?.();
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Cancellation failed');
        } finally {
            setLoading(false);
        }
    };

    const handleRefresh = async () => {
        try {
            setLoading(true);
            const refreshed = await bookingApiService.getBooking(booking.id);
            setCurrentBooking(refreshed);
            setVerifyState('Booking refreshed from service');
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Refresh failed');
        } finally {
            setLoading(false);
        }
    };

    const handleVerify = async () => {
        if (!booking.verificationToken) {
            setVerifyState('No token available for this booking');
            return;
        }

        try {
            setLoading(true);
            const valid = await bookingApiService.verifyBooking(
                booking.id,
                booking.verificationToken
            );
            setVerifyState(valid ? 'Verification passed' : 'Verification failed');
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Verification failed');
        } finally {
            setLoading(false);
        }
    };

    const handleLoadAudit = async () => {
        try {
            setLoading(true);
            const records = await auditApiService.getBookingAudit(booking.id);
            setAuditRecords(records);
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Failed to load audit trail');
        } finally {
            setLoading(false);
        }
    };

    const isCancellable =
        booking.state === 'CONFIRMED' &&
        new Date(booking.departureTime) > new Date();

    return (
        <div className="bg-traffic-panel border border-traffic-border p-6">
            <div className="mb-6">
                <h2 className="font-barlow font-bold text-lg uppercase tracking-wider text-traffic-white mb-2">
                    Booking Status
                </h2>
                <p className="font-mono text-xs text-traffic-text-2">
          // Booking ID: {booking.id}
                </p>
            </div>

            {/* Status Badge */}
            <div className="mb-6">
                <div
                    className={`inline-block border px-4 py-2 ${stateColors[booking.state]}`}
                >
                    <p className="font-mono font-bold uppercase text-sm tracking-widest">
                        {booking.state}
                    </p>
                    <p className="font-mono text-xs mt-1">
                        {stateDescription[booking.state]}
                    </p>
                </div>
            </div>

            {/* Journey Details */}
            <div className="space-y-4 mb-6 border-t border-traffic-border pt-4">
                <div className="grid grid-cols-2 gap-4">
                    <div>
                        <p className="font-mono text-xs text-traffic-text-3 uppercase mb-1">
                            Origin
                        </p>
                        <p className="text-traffic-white font-semibold">{booking.origin}</p>
                    </div>
                    <div>
                        <p className="font-mono text-xs text-traffic-text-3 uppercase mb-1">
                            Destination
                        </p>
                        <p className="text-traffic-white font-semibold">
                            {booking.destination}
                        </p>
                    </div>
                </div>

                <div className="grid grid-cols-2 gap-4">
                    <div>
                        <p className="font-mono text-xs text-traffic-text-3 uppercase mb-1">
                            Departure
                        </p>
                        <p className="text-traffic-accent font-mono text-sm">
                            {new Date(booking.departureTime).toLocaleString()}
                        </p>
                    </div>
                    <div>
                        <p className="font-mono text-xs text-traffic-text-3 uppercase mb-1">
                            Estimated Arrival
                        </p>
                        <p className="text-traffic-accent font-mono text-sm">
                            {new Date(booking.arrivalTime).toLocaleString()}
                        </p>
                    </div>
                </div>
            </div>

            {/* Segments */}
            {booking.segments.length > 0 && (
                <div className="mb-6 border-t border-traffic-border pt-4">
                    <p className="font-mono text-xs text-traffic-text-3 uppercase mb-3">
                        Route Segments
                    </p>
                    <div className="space-y-2">
                        {booking.segments.map((segment: RouteSegment) => (
                            <div
                                key={segment.id}
                                className="bg-traffic-bg p-3 border border-traffic-border text-xs font-mono"
                            >
                                <div className="flex justify-between items-start mb-2">
                                    <span className="text-traffic-accent font-bold">
                                        {segment.id}
                                    </span>
                                    <span className="text-traffic-text-2">
                                        {segment.ownerRegion}
                                    </span>
                                </div>
                                <p className="text-traffic-text-2 mb-2">{segment.name}</p>
                                <div className="flex justify-between text-traffic-text-3">
                                    <span>Capacity: {segment.available}/{segment.capacity}</span>
                                    {segment.closure && (
                                        <span className="text-traffic-red">CLOSED</span>
                                    )}
                                    {segment.restriction && (
                                        <span className="text-traffic-amber">{segment.restriction}</span>
                                    )}
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            )}

            {/* Verification Token */}
            {booking.verificationToken && booking.state === 'CONFIRMED' && (
                <div className="mb-6 border-t border-traffic-border pt-4">
                    <p className="font-mono text-xs text-traffic-text-3 uppercase mb-3">
                        Verification Token
                    </p>
                    <div className="bg-traffic-bg p-4 border border-traffic-accent">
                        <p className="font-mono text-sm text-traffic-accent font-bold tracking-widest">
                            {booking.verificationToken}
                        </p>
                        <p className="font-mono text-xs text-traffic-text-2 mt-2">
                            Show to enforcement agents for verification
                        </p>
                    </div>
                </div>
            )}

            {/* Actions */}
            {booking.state === 'CONFIRMED' && (
                <div className="space-y-3">
                    <Button onClick={handleRefresh} className="w-full">
                        Refresh Booking
                    </Button>
                    <Button onClick={handleVerify} className="w-full">
                        Verify Token
                    </Button>
                    <Button onClick={handleLoadAudit} className="w-full">
                        Load Booking Audit
                    </Button>
                    {isCancellable && (
                        <Button
                            onClick={handleCancel}
                            className="w-full"
                            variant="primary"
                        >
                            Cancel Booking
                        </Button>
                    )}
                    {!isCancellable && (
                        <p className="text-xs text-traffic-text-2 font-mono text-center">
                            Cancellation no longer available (within 24 hours of departure)
                        </p>
                    )}
                </div>
            )}

            {verifyState && (
                <p className="mt-4 text-xs font-mono text-traffic-text-2">{verifyState}</p>
            )}

            {auditRecords.length > 0 && (
                <div className="mt-4 border-t border-traffic-border pt-4 space-y-2">
                    <p className="font-mono text-xs text-traffic-text-3 uppercase">
                        Audit Events
                    </p>
                    {auditRecords.slice(0, 5).map((record) => (
                        <div
                            key={record.id}
                            className="bg-traffic-bg border border-traffic-border p-2"
                        >
                            <p className="font-mono text-xs text-traffic-accent">
                                {record.eventType}
                            </p>
                            <p className="font-mono text-xs text-traffic-text-3">
                                {record.createdAt
                                    ? new Date(record.createdAt).toLocaleString()
                                    : 'No timestamp'}
                            </p>
                        </div>
                    ))}
                </div>
            )}

            {booking.state === 'REJECTED' && (
                <div className="bg-traffic-red/10 border border-traffic-red p-4">
                    <p className="text-traffic-red font-mono text-sm">
                        ✗ Booking rejected due to insufficient capacity on one or more
                        segments. Try a different time window.
                    </p>
                </div>
            )}
        </div>
    );
}
