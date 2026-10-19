import { StatCard } from '../common/StatCard';

interface StatsRowProps {
    permitsGranted: number;
    permitsGrantedDelta: string;
    permitsDenied: number;
    permitsDeniedDelta: string;
    pendingConsensus: number;
    pendingConsensusDelta: string;
    globalCongestion: number;
    globalCongestionDelta: string;
}

export function StatsRow({
    permitsGranted,
    permitsGrantedDelta,
    permitsDenied,
    permitsDeniedDelta,
    pendingConsensus,
    pendingConsensusDelta,
    globalCongestion,
    globalCongestionDelta,
}: StatsRowProps) {
    return (
        <div className="grid grid-cols-4 gap-3 mb-6">
            <StatCard
                label="Permits Granted"
                value={permitsGranted.toLocaleString()}
                delta={permitsGrantedDelta}
                color="green"
            />
            <StatCard
                label="Permits Denied"
                value={permitsDenied.toLocaleString()}
                delta={permitsDeniedDelta}
                color="red"
            />
            <StatCard
                label="Pending Consensus"
                value={pendingConsensus}
                delta={pendingConsensusDelta}
                color="amber"
            />
            <StatCard
                label="Global Congestion"
                value={`${globalCongestion}%`}
                delta={globalCongestionDelta}
                color="blue"
            />
        </div>
    );
}
