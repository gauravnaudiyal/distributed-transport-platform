import type { PermitItem } from '../../types/index';
import { cn } from '../../utils/cn';

interface PermitCardProps {
    permit: PermitItem;
    onClick?: () => void;
}

export function PermitCard({ permit, onClick }: PermitCardProps) {
    const borderClasses =
        permit.status === 'approved'
            ? 'border-l-4 border-l-traffic-green'
            : permit.status === 'denied'
                ? 'border-l-4 border-l-traffic-red-2'
                : 'border-l-4 border-l-traffic-amber-2';

    const statusBgClasses =
        permit.status === 'approved'
            ? 'border border-traffic-green text-traffic-green bg-traffic-bg'
            : permit.status === 'denied'
                ? 'border border-traffic-red-2 text-traffic-red-2 bg-traffic-bg'
                : 'border border-traffic-amber-2 text-traffic-amber-2 bg-traffic-bg';

    const statusText = permit.status.charAt(0).toUpperCase() + permit.status.slice(1);

    return (
        <div
            onClick={onClick}
            className={cn(
                'border border-traffic-border p-3.5 mb-2.5 cursor-pointer transition-colors duration-200',
                'hover:border-traffic-border-2',
                borderClasses
            )}
        >
            <div className="flex items-center justify-between mb-1.5">
                <div className="font-barlow font-bold text-sm uppercase tracking-wide text-traffic-white">
                    {permit.route}
                </div>
                <div className={cn('font-mono text-xs uppercase tracking-widest px-2 py-0.5', statusBgClasses)}>
                    {statusText}
                </div>
            </div>

            <div className="font-mono text-xs text-traffic-text-3 flex gap-4 mb-1">
                {permit.departureTime && <span>{permit.departureTime}</span>}
                {permit.eta && <span>ETA {permit.eta}</span>}
                {permit.vehicleType && <span>{permit.vehicleType}</span>}
            </div>

            <div className="font-mono text-xs text-traffic-text-2 mt-1">
                {permit.id} · {permit.issuedAt ? `Issued ${permit.issuedAt}` : ''}
            </div>

            {permit.reason && (
                <div className="font-mono text-xs text-traffic-text-2 mt-1">{permit.reason}</div>
            )}
            {permit.alternative && (
                <div className="font-mono text-xs text-traffic-text-2 mt-1">{permit.alternative}</div>
            )}
        </div>
    );
}
