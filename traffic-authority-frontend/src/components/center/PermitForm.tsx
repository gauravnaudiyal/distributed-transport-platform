import type { UserPermitRequest } from '../../types/index';
import { cn } from '../../utils/cn';

interface PermitFormProps {
    request: UserPermitRequest;
}

export function PermitForm({ request }: PermitFormProps) {
    return (
        <div className="grid grid-cols-2 gap-5">
            {/* Left Column - Form Fields */}
            <div className="space-y-3.5">
                <div>
                    <div className="font-mono text-xs text-traffic-text-3 uppercase tracking-widest mb-1.5">
                        Origin Sector
                    </div>
                    <input
                        type="text"
                        value={request.originSector}
                        readOnly
                        className="w-full bg-traffic-bg border border-traffic-border-2 text-traffic-text px-3 py-2 font-mono text-xs uppercase tracking-wide outline-none focus:border-traffic-blue"
                    />
                </div>

                <div>
                    <div className="font-mono text-xs text-traffic-text-3 uppercase tracking-widest mb-1.5">
                        Destination Sector
                    </div>
                    <input
                        type="text"
                        value={request.destinationSector}
                        readOnly
                        className="w-full bg-traffic-bg border border-traffic-border-2 text-traffic-text px-3 py-2 font-mono text-xs uppercase tracking-wide outline-none focus:border-traffic-blue"
                    />
                </div>

                <div>
                    <div className="font-mono text-xs text-traffic-text-3 uppercase tracking-widest mb-1.5">
                        Vehicle Type
                    </div>
                    <input
                        type="text"
                        value={request.vehicleType}
                        readOnly
                        className="w-full bg-traffic-bg border border-traffic-border-2 text-traffic-text px-3 py-2 font-mono text-xs uppercase tracking-wide outline-none focus:border-traffic-blue"
                    />
                </div>

                <div>
                    <div className="font-mono text-xs text-traffic-text-3 uppercase tracking-widest mb-1.5">
                        Occupancy
                    </div>
                    <input
                        type="text"
                        value={request.occupancy}
                        readOnly
                        className="w-full bg-traffic-bg border border-traffic-border-2 text-traffic-text px-3 py-2 font-mono text-xs uppercase tracking-wide outline-none focus:border-traffic-blue"
                    />
                </div>
            </div>

            {/* Right Column - Compatibility Checks */}
            <div>
                <div className="font-mono text-xs text-traffic-text-3 uppercase tracking-widest mb-2.5 flex items-center gap-2.5">
                    <span>Compatibility Checks</span>
                    <div className="flex-1 h-px bg-traffic-border" />
                </div>

                <div className="space-y-2">
                    <div className="flex items-center gap-2.5 font-mono text-xs text-traffic-text-2 px-3 py-2 bg-traffic-bg/30 border border-traffic-border">
                        <span className="text-traffic-green">✓</span>
                        <span>Corridor capacity available (68%)</span>
                    </div>
                    <div className="flex items-center gap-2.5 font-mono text-xs text-traffic-text-2 px-3 py-2 bg-traffic-bg/30 border border-traffic-border">
                        <span className="text-traffic-green">✓</span>
                        <span>No conflicting reservations</span>
                    </div>
                    <div className="flex items-center gap-2.5 font-mono text-xs text-traffic-text-2 px-3 py-2 bg-traffic-bg/30 border border-traffic-border">
                        <span className="text-traffic-amber-2">⚠</span>
                        <span>Peak-hour surcharge applies</span>
                    </div>
                    <div className="flex items-center gap-2.5 font-mono text-xs text-traffic-text-2 px-3 py-2 bg-traffic-bg/30 border border-traffic-border">
                        <span className="text-traffic-green">✓</span>
                        <span>Citizen clearance sufficient</span>
                    </div>
                    <div className="flex items-center gap-2.5 font-mono text-xs text-traffic-text-2 px-3 py-2 bg-traffic-bg/30 border border-traffic-border">
                        <span className="text-traffic-amber-2">⚠</span>
                        <span>EU-WEST gateway degraded — possible delay</span>
                    </div>
                </div>
            </div>
        </div>
    );
}
