import { cn } from '../../utils/cn';

interface HealthDotProps {
    status: 'green' | 'amber' | 'red';
    className?: string;
}

export function HealthDot({ status, className }: HealthDotProps) {
    return (
        <div className={cn('health-dot', status, className)} />
    );
}
