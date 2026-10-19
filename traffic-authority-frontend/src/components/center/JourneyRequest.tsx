import { Stepper } from './Stepper';
import { PermitForm } from './PermitForm';
import { PermitResult } from './PermitResult';
import type { UserPermitRequest } from '../../types/index';
import { useUIStore } from '../../store/ui';

interface JourneyRequestProps {
    request: UserPermitRequest;
}

export function JourneyRequest({ request }: JourneyRequestProps) {
    const { formStep, setFormStep } = useUIStore();

    return (
        <>
            <div className="font-mono text-xs text-traffic-text-3 uppercase tracking-widest border-t border-traffic-border pt-5 mt-2 mb-4">
        // New Journey Request — Permit Application
            </div>

            <div className="bg-traffic-panel border border-traffic-border p-5 mb-4">
                <Stepper currentStep={formStep} onStepChange={setFormStep} />

                <PermitForm request={request} />

                <div className="grid grid-cols-2 gap-4 mt-5">
                    <PermitResult status="approved" />
                    <PermitResult status="denied" />
                </div>
            </div>
        </>
    );
}
