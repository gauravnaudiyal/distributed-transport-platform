import { Button } from '../common/Button';

export function Footer() {
    return (
        <div className="bg-traffic-panel border-t border-traffic-border px-8 py-2 flex items-center justify-between font-mono text-xs text-traffic-text-3 uppercase tracking-wide">
            <div>THE TRAFFIC AUTHORITY © SECTOR 7 — ALL MOVEMENT REQUIRES AUTHORIZATION</div>
            <div>NODE: EU-WEST-NODE-04 · BUILD: 2.14.1-rc · UPTIME: 99.1%</div>
            <div className="text-traffic-amber-2">⚠ DEGRADED: EU-W, AP-S</div>
        </div>
    );
}
