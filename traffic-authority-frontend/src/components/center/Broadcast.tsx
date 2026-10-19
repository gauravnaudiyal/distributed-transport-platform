import { Button } from '../common/Button';

export function Broadcast() {
    return (
        <div className="bg-traffic-amber/8 border border-traffic-amber/30 border-l-4 border-l-traffic-amber-2 px-4 py-2.5 font-mono text-xs text-traffic-amber-2 uppercase tracking-wider mb-7 flex items-center gap-3">
            <span className="text-sm animate-amb-pulse">⚠</span>
            <span>
                Authority Broadcast — Peak-hour restrictions active in EU-WEST. Priority permits only until 10:00.
                AP-SOUTH gateway offline. Affected journeys rerouted via NA-EAST relay.
            </span>
        </div>
    );
}
