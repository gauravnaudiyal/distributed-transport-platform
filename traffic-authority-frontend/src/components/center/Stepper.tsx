import { cn } from '../../utils/cn';
import { Button } from '../common/Button';

interface StepperProps {
    currentStep: 1 | 2 | 3 | 4;
    onStepChange: (step: 1 | 2 | 3 | 4) => void;
}

export function Stepper({ currentStep, onStepChange }: StepperProps) {
    const steps = [
        { num: 1, label: 'Route' },
        { num: 2, label: 'Time Window' },
        { num: 3, label: 'Vehicle' },
        { num: 4, label: 'Review' },
    ];

    return (
        <div className="flex gap-0 mb-6">
            {steps.map((step) => {
                const isActive = currentStep === step.num;
                const isDone = step.num < currentStep;

                return (
                    <div
                        key={step.num}
                        onClick={() => onStepChange(step.num as 1 | 2 | 3 | 4)}
                        className={cn(
                            'flex-1 py-2.5 text-center font-mono text-xs uppercase tracking-widest cursor-pointer transition-all',
                            'border-b-2',
                            isDone
                                ? 'text-traffic-green-2 border-b-traffic-green-2'
                                : isActive
                                    ? 'text-traffic-white border-b-traffic-red'
                                    : 'text-traffic-text-3 border-b-traffic-border'
                        )}
                    >
                        <div className="text-sm font-barlow font-black mb-0.5">{step.num}</div>
                        <div>{step.label}</div>
                    </div>
                );
            })}
        </div>
    );
}
