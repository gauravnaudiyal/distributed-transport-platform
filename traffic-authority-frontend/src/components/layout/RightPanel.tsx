import { CapacityBar } from '../common/CapacityBar';
import type { RegionalCapacity, CongestedCorridor } from '../../types/index';

interface RightPanelProps {
    regionalCapacity: RegionalCapacity[];
    congestedCorridors: CongestedCorridor[];
    globalCongestion: number;
}

export function RightPanel({
    regionalCapacity,
    congestedCorridors,
    globalCongestion,
}: RightPanelProps) {
    return (
        <div className="bg-traffic-panel border-l border-traffic-border w-72 overflow-y-auto flex flex-col">
            {/* Regional Capacity */}
            <div className="border-b border-traffic-border p-5">
                <div className="font-mono text-xs text-traffic-text-3 uppercase tracking-widest mb-3.5">
          // Regional Capacity
                </div>
                <div className="grid grid-cols-2 gap-2">
                    {regionalCapacity.map((region) => (
                        <div key={region.region} className="border border-traffic-border p-3 cursor-pointer hover:border-traffic-border-2">
                            <div className="font-mono text-xs text-traffic-text-2 uppercase tracking-wide mb-2">
                                {region.region}
                            </div>
                            <div className="h-1 bg-traffic-border mb-1.5">
                                <div
                                    className={`h-full transition-all ${region.status === 'green'
                                            ? 'bg-traffic-green'
                                            : region.status === 'amber'
                                                ? 'bg-traffic-amber-2'
                                                : 'bg-traffic-red-2'
                                        }`}
                                    style={{ width: `${region.capacity}%` }}
                                />
                            </div>
                            <div
                                className={`font-mono text-xs font-bold tracking-wide ${region.status === 'green'
                                        ? 'text-traffic-green'
                                        : region.status === 'amber'
                                            ? 'text-traffic-amber-2'
                                            : 'text-traffic-red-2'
                                    }`}
                            >
                                {region.offline ? 'OFFLINE' : `${region.capacity}%`}
                            </div>
                        </div>
                    ))}
                </div>
            </div>

            {/* Top Congested Corridors */}
            <div className="border-b border-traffic-border p-5">
                <div className="font-mono text-xs text-traffic-text-3 uppercase tracking-widest mb-3.5">
          // Top Congested Corridors
                </div>
                <div className="space-y-3">
                    {congestedCorridors.map((corridor) => (
                        <CapacityBar
                            key={corridor.route}
                            name={corridor.route}
                            percentage={corridor.capacity}
                            color={corridor.status}
                        />
                    ))}
                </div>
            </div>

            {/* Congestion Index */}
            <div className="border-b border-traffic-border p-5">
                <div className="font-mono text-xs text-traffic-text-3 uppercase tracking-widest mb-3">
          // Congestion Index
                </div>
                <svg width="100%" height="60" viewBox="0 0 240 60" className="w-full">
                    <polyline
                        points="0,50 20,45 40,40 60,42 80,35 100,30 120,38 140,25 160,22 180,30 200,18 220,24 240,15"
                        fill="none"
                        stroke="#00ff88"
                        strokeWidth="1.5"
                        opacity="0.6"
                    />
                    <polyline
                        points="0,50 20,45 40,40 60,42 80,35 100,30 120,38 140,25 160,22 180,30 200,18 220,24 240,15"
                        fill="rgba(0,255,136,0.05)"
                        stroke="none"
                    />
                    <circle cx="240" cy="15" r="3" fill="#00ff88" opacity="0.9">
                        <animate attributeName="r" values="2;4;2" dur="2s" repeatCount="indefinite" />
                        <animate attributeName="opacity" values="1;0.4;1" dur="2s" repeatCount="indefinite" />
                    </circle>
                </svg>
                <div className="font-mono text-xs text-traffic-text-3 text-right mt-1">
                    LIVE · GLOBAL AVG: {globalCongestion}%
                </div>
            </div>

            {/* Sanctions & Warnings */}
            <div className="p-5 flex-1">
                <div className="font-mono text-xs text-traffic-text-3 uppercase tracking-widest mb-3.5">
          // Sanctions & Warnings
                </div>
                <div className="font-mono text-xs space-y-2">
                    <div className="text-traffic-amber-2">⚠ 2 denied requests this week</div>
                    <div className="text-traffic-text-3">No active sanctions</div>
                    <div className="mt-2 text-traffic-text-3">Compliance score:</div>
                    <div className="text-traffic-green text-lg font-bold tracking-wide">98.2%</div>
                </div>
            </div>
        </div>
    );
}
