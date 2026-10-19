import { Button } from '../common/Button';
import { cn } from '../../utils/cn';

interface PermitResultProps {
    status: 'approved' | 'denied';
}

export function PermitResult({ status }: PermitResultProps) {
    if (status === 'approved') {
        return (
            <div className="border border-traffic-green bg-traffic-green/3 p-5 relative overflow-hidden">
                <div
                    className="absolute inset-0 pointer-events-none"
                    style={{
                        background: 'linear-gradient(135deg, rgba(0,255,136,0.05) 0%, transparent 50%)',
                    }}
                />

                <div className="flex items-start justify-between mb-3 relative z-10">
                    <div>
                        <div className="font-mono text-lg text-traffic-green uppercase tracking-widest font-bold animate-glow">
                            PERMIT GRANTED
                        </div>
                        <div className="font-mono text-xs text-traffic-text-3 uppercase tracking-wide mt-1">
                            PROCEED WITHIN ASSIGNED WINDOW
                        </div>
                    </div>

                    {/* QR Placeholder */}
                    <div className="w-16 h-16 border-2 border-traffic-green grid grid-cols-8 grid-rows-8 gap-0.5 p-1 opacity-70">
                        {Array.from({ length: 64 }).map((_, i) => (
                            <div
                                key={i}
                                className={cn(
                                    'bg-traffic-green',
                                    Math.random() > 0.5 ? 'opacity-100' : 'opacity-20'
                                )}
                            />
                        ))}
                    </div>
                </div>

                <div className="space-y-1.5 font-mono text-xs relative z-10">
                    <div className="flex justify-between">
                        <span className="text-traffic-text-3 uppercase tracking-widest">Permit ID</span>
                        <span className="text-traffic-text">PERMIT-9912</span>
                    </div>
                    <div className="flex justify-between">
                        <span className="text-traffic-text-3 uppercase tracking-widest">Departure</span>
                        <span className="text-traffic-green">08:20 — 08:35</span>
                    </div>
                    <div className="flex justify-between">
                        <span className="text-traffic-text-3 uppercase tracking-widest">Route</span>
                        <span className="text-traffic-text">LDN → AMS</span>
                    </div>
                    <div className="flex justify-between">
                        <span className="text-traffic-text-3 uppercase tracking-widest">Expires</span>
                        <span className="text-traffic-text">08:35 TODAY</span>
                    </div>
                </div>

                <div className="flex gap-2 mt-3.5 relative z-10">
                    <Button variant="ghost" size="sm">
                        Download Token
                    </Button>
                    <Button variant="danger" size="sm">
                        Cancel
                    </Button>
                </div>
            </div>
        );
    }

    return (
        <div className="border border-traffic-red-2 bg-traffic-red-2/3 p-5 relative overflow-hidden">
            <div
                className="absolute inset-0 pointer-events-none"
                style={{
                    background: 'linear-gradient(135deg, rgba(231,76,60,0.05) 0%, transparent 50%)',
                }}
            />

            <div className="flex items-start justify-between mb-3 relative z-10">
                <div>
                    <div className="font-mono text-lg text-traffic-red-2 uppercase tracking-widest font-bold">
                        PERMIT DENIED
                    </div>
                    <div className="font-mono text-xs text-traffic-text-3 uppercase tracking-wide mt-1">
                        AUTHORIZATION REFUSED
                    </div>
                </div>
                <div className="text-4xl opacity-20 text-traffic-red-2">✗</div>
            </div>

            <div className="font-mono text-xs text-traffic-red-2 uppercase tracking-wide mb-3 relative z-10">
                Reasons for Denial:
            </div>

            <div className="font-mono text-xs text-traffic-text-2 leading-relaxed mb-3 relative z-10">
                · Corridor capacity exceeded (94%)<br />
                · Peak-hour restrictions: CIVILIAN tier blocked<br />
                · Requested window unavailable
            </div>

            <div className="font-mono text-xs text-traffic-amber-2 uppercase tracking-wide mb-1.5 relative z-10">
                Suggested Alternatives:
            </div>

            <div className="font-mono text-xs text-traffic-text-2 leading-relaxed mb-3.5 relative z-10">
                → 11:00 window available (38% capacity)<br />
                → Alternate route via NA-EAST relay
            </div>

            <div className="flex gap-2 relative z-10">
                <Button variant="ghost" size="sm">
                    Resubmit
                </Button>
            </div>
        </div>
    );
}
