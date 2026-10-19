import { cn } from '../../utils/cn';

interface CapacityBarProps {
    name: string;
    percentage: number;
    color: 'green' | 'amber' | 'red';
    className?: string;
}

export function CapacityBar({ name, percentage, color, className }: CapacityBarProps) {
    const fillClasses =
        color === 'green'
            ? 'bg-traffic-green shadow-green-glow'
            : color === 'amber'
                ? 'bg-traffic-amber-2'
                : 'bg-traffic-red-2 shadow-red-glow';

    const percentageClasses =
        color === 'green'
            ? 'text-traffic-green'
            : color === 'amber'
                ? 'text-traffic-amber-2'
                : 'text-traffic-red-2';

    return (
        <div className={className}>
            <div className="flex justify-between items-center mb-1.5 font-mono text-xs">
                <span className="text-traffic-text-2 tracking-wide">{name}</span>
                <span className={cn('text-traffic-text-3', percentageClasses)}>
                    {percentage}%
                </span>
            </div>
            <div className="h-1 bg-traffic-border relative overflow-hidden group">
                <div
                    className={cn('h-full transition-all duration-500', fillClasses)}
                    style={{ width: `${percentage}%` }}
                >
                    <div className="absolute inset-0 bg-gradient-to-r from-transparent via-white to-transparent opacity-0 group-hover:opacity-30 animate-shimmer" />
                </div>
            </div>
        </div>
    );
}
