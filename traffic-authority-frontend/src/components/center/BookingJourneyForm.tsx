import { useEffect, useMemo, useState } from 'react';
import { useBookingStore } from '../../store/booking';
import { bookingApiService } from '../../services/bookingApi';
import { compatibilityApiService } from '../../services/compatibilityApi';
import { routeApiService } from '../../services/routeApi';
import { useAuth } from '../../context/AuthContext';
import { useAuthorityStore } from '../../store/authority';
import { Button } from '../common/Button';
import type { RouteSegment } from '../../types/route';

export function BookingJourneyForm() {
    const {
        setCurrentBooking,
        addBooking,
        setLoading,
        setError,
        setCompatibilityResult,
        setRoutePreview,
    } = useBookingStore();
    const { userId } = useAuth();
    const { isPolicyUpdateInFlight } = useAuthorityStore();
    const [origin, setOrigin] = useState('');
    const [destination, setDestination] = useState('');
    const [routeNodesLoading, setRouteNodesLoading] = useState(true);
    const [routeNodesError, setRouteNodesError] = useState<string | null>(null);
    const [segments, setSegments] = useState<RouteSegment[]>([]);
    const [departureTime, setDepartureTime] = useState(
        new Date(Date.now() + 24 * 3600000).toISOString().split('T')[0]
    );
    const [departureHour, setDepartureHour] = useState('09');
    const [vehicleType, setVehicleType] = useState<'CAR' | 'BUS' | 'TRUCK'>('CAR');
    const [passengerCount, setPassengerCount] = useState(1);
    const [isSubmitting, setIsSubmitting] = useState(false);

    const nodeOptions = useMemo(() => {
        const allNodes = segments.flatMap((segment) => [
            segment.originNode,
            segment.destinationNode,
        ]);
        return [...new Set(allNodes)].sort((a, b) => a.localeCompare(b));
    }, [segments]);

    const nodeInsights = useMemo(() => {
        const insights = new Map<string, { outbound: number; inbound: number; regions: Set<string> }>();
        for (const segment of segments) {
            if (!insights.has(segment.originNode)) {
                insights.set(segment.originNode, {
                    outbound: 0,
                    inbound: 0,
                    regions: new Set<string>(),
                });
            }
            if (!insights.has(segment.destinationNode)) {
                insights.set(segment.destinationNode, {
                    outbound: 0,
                    inbound: 0,
                    regions: new Set<string>(),
                });
            }

            const originInsight = insights.get(segment.originNode);
            const destinationInsight = insights.get(segment.destinationNode);
            if (originInsight) {
                originInsight.outbound += 1;
                originInsight.regions.add(segment.authoritativeRegion);
            }
            if (destinationInsight) {
                destinationInsight.inbound += 1;
                destinationInsight.regions.add(segment.authoritativeRegion);
            }
        }
        return insights;
    }, [segments]);

    useEffect(() => {
        const loadSegments = async () => {
            try {
                setRouteNodesLoading(true);
                setRouteNodesError(null);
                const response = await routeApiService.getSegments();
                setSegments(response);
                const uniqueNodes = [
                    ...new Set(
                        response.flatMap((segment) => [
                            segment.originNode,
                            segment.destinationNode,
                        ])
                    ),
                ].sort((a, b) => a.localeCompare(b));

                if (uniqueNodes.length > 0) {
                    setOrigin(uniqueNodes[0] || '');
                    setDestination(uniqueNodes[1] || uniqueNodes[0] || '');
                }
            } catch (err) {
                setRouteNodesError(
                    err instanceof Error
                        ? err.message
                        : 'Failed to load route nodes from route management'
                );
            } finally {
                setRouteNodesLoading(false);
            }
        };

        void loadSegments();
    }, []);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!origin || !destination) {
            setError('Please choose both start and destination nodes.');
            return;
        }
        if (origin === destination) {
            setError('Start and destination cannot be the same node.');
            return;
        }
        setIsSubmitting(true);
        setError(null);

        try {
            setLoading(true);
            setCompatibilityResult(null);
            setRoutePreview(null);

            const fullDepartureTime = new Date(
                `${departureTime}T${departureHour}:00:00`
            ).toISOString();
            const currentMapVersion = await routeApiService.getCurrentMapVersion();
            const routePreview = await routeApiService.decomposeRoute({
                origin,
                destination,
                mapVersion: currentMapVersion.version,
            });
            setRoutePreview(routePreview);

            const compatibility = await compatibilityApiService.checkCompatibility({
                bookingId: `PRECHECK-${Date.now()}`,
                routeId: `${origin}-${destination}`,
                routeName: `${origin} to ${destination}`,
                travelDate: departureTime,
                travelTime: `${departureHour}:00:00`,
                vehicleType,
                passengerCount,
                driverId: userId || 'civilian-demo',
            });

            setCompatibilityResult(compatibility);
            if (compatibility.status !== 'APPROVED') {
                throw new Error(
                    compatibility.message ||
                    'Journey is not compatible for this time window'
                );
            }

            const booking = await bookingApiService.requestBooking({
                origin,
                destination,
                departureTime: fullDepartureTime,
                timeWindow: 60,
            });

            addBooking(booking);
            setCurrentBooking(booking);
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Booking failed');
        } finally {
            setLoading(false);
            setIsSubmitting(false);
        }
    };

    return (
        <div className="bg-traffic-panel border border-traffic-border p-6">
            <div className="mb-6">
                <h2 className="font-barlow font-bold text-lg uppercase tracking-wider text-traffic-white mb-2">
                    Request Journey Booking
                </h2>
                <p className="font-mono text-xs text-traffic-text-2">
          // Select route and departure time
                </p>
            </div>

            <form onSubmit={handleSubmit} className="space-y-4">
                {/* Start and Destination Selection */}
                <div className="grid grid-cols-2 gap-4">
                    <div>
                        <label className="block font-mono text-xs text-traffic-text-3 uppercase mb-2">
                            Start Node
                        </label>
                        <select
                            value={origin}
                            onChange={(e) => setOrigin(e.target.value)}
                            disabled={routeNodesLoading || nodeOptions.length === 0}
                            className="w-full bg-traffic-bg border border-traffic-accent text-traffic-text px-3 py-2 font-mono text-sm disabled:opacity-60"
                        >
                            {nodeOptions.map((node) => (
                                <option key={`origin-${node}`} value={node}>
                                    {node}
                                </option>
                            ))}
                        </select>
                        {origin && nodeInsights.get(origin) && (
                            <p className="mt-2 font-mono text-xs text-traffic-text-3">
                                Outbound: {nodeInsights.get(origin)?.outbound} | Inbound:{' '}
                                {nodeInsights.get(origin)?.inbound} | Regions:{' '}
                                {Array.from(nodeInsights.get(origin)?.regions || []).join(', ')}
                            </p>
                        )}
                    </div>
                    <div>
                        <label className="block font-mono text-xs text-traffic-text-3 uppercase mb-2">
                            Destination Node
                        </label>
                        <select
                            value={destination}
                            onChange={(e) => setDestination(e.target.value)}
                            disabled={routeNodesLoading || nodeOptions.length === 0}
                            className="w-full bg-traffic-bg border border-traffic-accent text-traffic-text px-3 py-2 font-mono text-sm disabled:opacity-60"
                        >
                            {nodeOptions.map((node) => (
                                <option key={`destination-${node}`} value={node}>
                                    {node}
                                </option>
                            ))}
                        </select>
                        {destination && nodeInsights.get(destination) && (
                            <p className="mt-2 font-mono text-xs text-traffic-text-3">
                                Outbound: {nodeInsights.get(destination)?.outbound} | Inbound:{' '}
                                {nodeInsights.get(destination)?.inbound} | Regions:{' '}
                                {Array.from(nodeInsights.get(destination)?.regions || []).join(', ')}
                            </p>
                        )}
                    </div>
                </div>
                <p className="font-mono text-xs text-traffic-text-3">
                    Nodes are route-network waypoints from Route Management, not street addresses.
                </p>
                {routeNodesLoading && (
                    <p className="font-mono text-xs text-traffic-text-3">
                        Loading available route nodes...
                    </p>
                )}
                {routeNodesError && (
                    <p className="font-mono text-xs text-traffic-red">{routeNodesError}</p>
                )}

                {/* Date and Time */}
                <div className="grid grid-cols-2 gap-4">
                    <div>
                        <label className="block font-mono text-xs text-traffic-text-3 uppercase mb-2">
                            Departure Date
                        </label>
                        <input
                            type="date"
                            value={departureTime}
                            onChange={(e) => setDepartureTime(e.target.value)}
                            className="w-full bg-traffic-bg border border-traffic-accent text-traffic-text px-3 py-2 font-mono text-sm"
                        />
                    </div>

                    <div>
                        <label className="block font-mono text-xs text-traffic-text-3 uppercase mb-2">
                            Departure Time
                        </label>
                        <select
                            value={departureHour}
                            onChange={(e) => setDepartureHour(e.target.value)}
                            className="w-full bg-traffic-bg border border-traffic-accent text-traffic-text px-3 py-2 font-mono text-sm"
                        >
                            {Array.from({ length: 24 }).map((_, i) => {
                                const hour = String(i).padStart(2, '0');
                                return (
                                    <option key={hour} value={hour}>
                                        {hour}:00
                                    </option>
                                );
                            })}
                        </select>
                    </div>
                </div>

                <div className="grid grid-cols-2 gap-4">
                    <div>
                        <label className="block font-mono text-xs text-traffic-text-3 uppercase mb-2">
                            Vehicle Type
                        </label>
                        <select
                            value={vehicleType}
                            onChange={(e) =>
                                setVehicleType(e.target.value as 'CAR' | 'BUS' | 'TRUCK')
                            }
                            className="w-full bg-traffic-bg border border-traffic-accent text-traffic-text px-3 py-2 font-mono text-sm"
                        >
                            <option value="CAR">CAR</option>
                            <option value="BUS">BUS</option>
                            <option value="TRUCK">TRUCK</option>
                        </select>
                    </div>
                    <div>
                        <label className="block font-mono text-xs text-traffic-text-3 uppercase mb-2">
                            Passenger Count
                        </label>
                        <input
                            type="number"
                            min={1}
                            max={100}
                            value={passengerCount}
                            onChange={(e) => setPassengerCount(Number(e.target.value) || 1)}
                            className="w-full bg-traffic-bg border border-traffic-accent text-traffic-text px-3 py-2 font-mono text-sm"
                        />
                    </div>
                </div>

                {/* Submit Button */}
                <Button
                    type="submit"
                    disabled={
                        isSubmitting ||
                        isPolicyUpdateInFlight ||
                        routeNodesLoading ||
                        nodeOptions.length === 0
                    }
                    className="w-full"
                    variant="primary"
                >
                    {isPolicyUpdateInFlight
                        ? 'Policy Sync In Progress...'
                        : isSubmitting
                            ? 'Checking Compatibility...'
                            : 'Request Booking'}
                </Button>
            </form>

            {/* Info Box */}
            <div className="mt-6 pt-6 border-t border-traffic-border text-xs text-traffic-text-2 font-mono space-y-1">
                <p>Booking latency: &lt; 800ms (single-region)</p>
                <p>Idempotency: Safe to retry</p>
                <p>Cancellation: Available until departure</p>
            </div>
        </div>
    );
}
