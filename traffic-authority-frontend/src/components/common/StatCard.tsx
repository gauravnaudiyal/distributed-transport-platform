import { cn } from '../../utils/cn';

interface StatCardProps {
    label: string;
    value: string | number;
    delta: string;
    color: 'green' | 'red' | 'amber' | 'blue';
}

export function StatCard({ label, value, delta, color }: StatCardProps) {
    const colorClasses =
        color === 'green'
            ? 'border-t-traffic-green'
            : color === 'red'
                ? 'border-t-traffic-red-2'
                : color === 'amber'
                    ? 'border-t-traffic-amber-2'
                    : 'border-t-traffic-blue';

    const valueClasses =
        color === 'green'
            ? 'text-traffic-green'
            : color === 'red'
                ? 'text-traffic-red-2'
                : color === 'amber'
                    ? 'text-traffic-amber-2'
                    : 'text-traffic-blue';

    return (
        <div
            className={cn(
                'bg-traffic-panel border border-traffic-border p-4',
                colorClasses,
                'border-t-4 relative overflow-hidden'
            )}
        >
            <div className="font-mono text-xs text-traffic-text-3 tracking-widest uppercase mb-2">
                {label}
            </div>
            <div className={cn('font-mono text-2xl font-bold mb-1', valueClasses)}>
                {value}
            </div>
            <div className="font-mono text-xs text-traffic-text-3">{delta}</div>
        </div>
    );
}
