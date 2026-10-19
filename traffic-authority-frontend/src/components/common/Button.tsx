import { ReactNode } from 'react';
import { cn } from '../../utils/cn';

interface ButtonProps {
    children: ReactNode;
    onClick?: () => void;
    variant?: 'primary' | 'ghost' | 'danger';
    size?: 'sm' | 'md';
    className?: string;
}

export function Button({
    children,
    onClick,
    variant = 'primary',
    size = 'md',
    className,
}: ButtonProps) {
    const baseClasses =
        variant === 'primary'
            ? 'btn-primary'
            : variant === 'danger'
                ? 'btn-ghost danger'
                : 'btn-ghost';

    const sizeClasses = size === 'sm' ? 'text-xs px-3 py-1.5' : '';

    return (
        <button onClick={onClick} className={cn(baseClasses, sizeClasses, className)}>
            {children}
        </button>
    );
}
