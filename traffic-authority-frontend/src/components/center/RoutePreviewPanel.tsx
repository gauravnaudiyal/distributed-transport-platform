import { useBookingStore } from '../../store/booking';

export function RoutePreviewPanel() {
    const { routePreview, compatibilityResult } = useBookingStore();

    if (!routePreview && !compatibilityResult) {
        return null;
    }

    return (
        <div className="bg-traffic-panel border border-traffic-border p-6">
            <h2 className="font-barlow font-bold text-lg uppercase tracking-wider text-traffic-white mb-2">
                Compatibility & Route Preview
            </h2>
            {compatibilityResult && (
                <div className="mb-4 text-sm font-mono">
                    <span className="text-traffic-text-2 mr-2">Status:</span>
                    <span
                        className={
                            compatibilityResult.status === 'APPROVED'
                                ? 'text-traffic-green'
                                : 'text-traffic-red'
                        }
                    >
                        {compatibilityResult.status}
                    </span>
                    <p className="text-traffic-text-3 mt-2">{compatibilityResult.message}</p>
                    <p className="text-traffic-text-3 mt-1">
                        Load {compatibilityResult.currentLoad}/{compatibilityResult.maxCapacity}
                    </p>
                </div>
            )}

            {routePreview && (
                <div>
                    <p className="font-mono text-xs text-traffic-text-3 uppercase mb-3">
                        Map Version: {routePreview.mapVersion}
                    </p>
                    <div className="space-y-2">
                        {routePreview.segments.map((segment) => (
                            <div
                                key={segment.segmentId}
                                className="bg-traffic-bg border border-traffic-border p-3 font-mono text-xs"
                            >
                                <p className="text-traffic-accent">{segment.segmentId}</p>
                                <p className="text-traffic-text-2">
                                    Region: {segment.authoritativeRegion}
                                </p>
                                <p className="text-traffic-text-3">
                                    Window: {new Date(segment.timeWindowStart).toLocaleString()} -{' '}
                                    {new Date(segment.timeWindowEnd).toLocaleString()}
                                </p>
                            </div>
                        ))}
                    </div>
                </div>
            )}
        </div>
    );
}
