import { useEffect, useState } from 'react';
import { routeApiService } from '../../services/routeApi';
import type { MapVersionInfo, RouteSegment } from '../../types/route';
import { Button } from '../common/Button';

export function TrafficMapPanel() {
    const [segments, setSegments] = useState<RouteSegment[]>([]);
    const [mapVersion, setMapVersion] = useState<MapVersionInfo | null>(null);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const loadMapData = async () => {
        try {
            setIsLoading(true);
            setError(null);
            const [currentVersion, allSegments] = await Promise.all([
                routeApiService.getCurrentMapVersion(),
                routeApiService.getSegments(),
            ]);
            setMapVersion(currentVersion);
            setSegments(allSegments);
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Failed to load traffic map');
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        void loadMapData();
    }, []);

    return (
        <div className="space-y-6">
            <div className="bg-traffic-panel border border-traffic-border p-6">
                <div className="flex items-center justify-between mb-3">
                    <h2 className="font-barlow font-bold text-lg uppercase tracking-wider text-traffic-white">
                        Traffic Map
                    </h2>
                    <Button onClick={loadMapData} disabled={isLoading}>
                        {isLoading ? 'Refreshing...' : 'Refresh Map'}
                    </Button>
                </div>
                <p className="font-mono text-xs text-traffic-text-3 uppercase">
                    {mapVersion
                        ? `Current map version: ${mapVersion.version}`
                        : 'Map version unavailable'}
                </p>
                {error && (
                    <p className="font-mono text-xs text-traffic-red mt-3">{error}</p>
                )}
            </div>

            <div className="bg-traffic-panel border border-traffic-border p-6">
                <p className="font-mono text-xs text-traffic-text-3 uppercase mb-3">
                    Segments ({segments.length})
                </p>
                <div className="space-y-2 max-h-[420px] overflow-y-auto">
                    {segments.map((segment) => (
                        <div
                            key={segment.segmentId}
                            className="bg-traffic-bg border border-traffic-border p-3"
                        >
                            <div className="flex items-center justify-between">
                                <p className="font-mono text-xs text-traffic-accent">
                                    {segment.segmentId}
                                </p>
                                <p className="font-mono text-xs text-traffic-text-2">
                                    {segment.active ? 'ACTIVE' : 'INACTIVE'}
                                </p>
                            </div>
                            <p className="font-mono text-xs text-traffic-text-2 mt-1">
                                {segment.originNode} {'->'} {segment.destinationNode}
                            </p>
                            <p className="font-mono text-xs text-traffic-text-3 mt-1">
                                Region: {segment.authoritativeRegion}
                            </p>
                            {segment.description && (
                                <p className="font-mono text-xs text-traffic-text-3 mt-1">
                                    {segment.description}
                                </p>
                            )}
                        </div>
                    ))}
                    {!isLoading && segments.length === 0 && (
                        <p className="font-mono text-xs text-traffic-text-2">
                            No segments returned by route service.
                        </p>
                    )}
                </div>
            </div>
        </div>
    );
}
