import { useState } from 'react';
import type { CapacityOverride } from '../../store/authority';
import { useAuthorityStore } from '../../store/authority';
import { authorityApiService } from '../../services/authorityApi';
import { Button } from '../common/Button';

export function CapacityOverridePanel() {
    const {
        overrides,
        addOverride,
        removeOverride,
        setPolicyUpdateInFlight,
        setLastPolicyApplyAt,
        setError,
        error,
    } = useAuthorityStore();
    const [isFormOpen, setIsFormOpen] = useState(false);
    const [segmentId, setSegmentId] = useState('SEG-UK-M1-001');
    const [overrideCapacity, setOverrideCapacity] = useState(150);
    const [reason, setReason] = useState('Event management');
    const [effectiveFrom, setEffectiveFrom] = useState(
        new Date().toISOString().split('T')[0]
    );
    const [effectiveUntil, setEffectiveUntil] = useState(
        new Date(Date.now() + 7 * 24 * 3600000).toISOString().split('T')[0]
    );

    const segmentOptions = [
        { id: 'SEG-UK-M1-001', name: 'M1 Motorway - North Section', capacity: 200 },
        { id: 'SEG-UK-M25-001', name: 'M25 Orbital - East Section', capacity: 180 },
        { id: 'SEG-FR-A6-001', name: 'Autoroute A6 - Île-de-France', capacity: 220 },
        { id: 'SEG-DE-A9-001', name: 'Autobahn A9 - Bavaria', capacity: 190 },
        { id: 'SEG-US-I95-001', name: 'Interstate 95 - Northeast Corridor', capacity: 300 },
    ];

    const currentSegment = segmentOptions.find((s) => s.id === segmentId);
    const originalCapacity = currentSegment?.capacity || 200;

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        const segment = segmentOptions.find((s) => s.id === segmentId);
        if (!segment) return;

        const override: CapacityOverride = {
            id: `OVERRIDE-${Date.now()}`,
            segmentId,
            segmentName: segment.name,
            originalCapacity: segment.capacity,
            overrideCapacity,
            reason,
            effectiveFrom,
            effectiveUntil,
            createdBy: 'Authority Admin',
            createdAt: new Date().toISOString(),
        };

        try {
            setPolicyUpdateInFlight(true);
            setError(null);
            await authorityApiService.createCapacityOverride({
                correlationId: `capacity-${Date.now()}`,
                jurisdictionId: 'UK-NORTH',
                segmentIds: [segmentId],
                effectiveFrom: new Date(effectiveFrom).toISOString(),
                effectiveUntil: new Date(effectiveUntil).toISOString(),
                capacityLimit: overrideCapacity,
                reason,
            });
            addOverride(override);
            setLastPolicyApplyAt(new Date().toISOString());
            setIsFormOpen(false);
        } catch (err) {
            setError(
                err instanceof Error ? err.message : 'Failed to apply capacity override'
            );
        } finally {
            setPolicyUpdateInFlight(false);
        }
    };

    const getCapacityTrend = (original: number, override: number) => {
        const diff = override - original;
        const percent = ((diff / original) * 100).toFixed(1);
        if (diff > 0) {
            return { text: `+${percent}%`, color: 'text-traffic-green' };
        } else {
            return { text: `${percent}%`, color: 'text-traffic-red' };
        }
    };

    return (
        <div className="bg-traffic-panel border border-traffic-border p-6">
            <div className="flex items-center justify-between mb-6">
                <div>
                    <h2 className="font-barlow font-bold text-lg uppercase tracking-wider text-traffic-white mb-1">
                        Capacity Overrides
                    </h2>
                    <p className="font-mono text-xs text-traffic-text-2">
            // Adjust segment capacities temporarily
                    </p>
                </div>
                {!isFormOpen && (
                    <Button onClick={() => setIsFormOpen(true)} variant="primary">
                        + New Override
                    </Button>
                )}
            </div>

            {isFormOpen && (
                <form onSubmit={handleSubmit} className="mb-6 p-4 bg-traffic-bg border border-traffic-accent space-y-4">
                    <div>
                        <label className="block font-mono text-xs text-traffic-text-3 uppercase mb-2">
                            Segment
                        </label>
                        <select
                            value={segmentId}
                            onChange={(e) => setSegmentId(e.target.value)}
                            className="w-full bg-traffic-bg border border-traffic-accent text-traffic-text px-3 py-2 font-mono text-sm"
                        >
                            {segmentOptions.map((seg) => (
                                <option key={seg.id} value={seg.id}>
                                    {seg.name} (Current: {seg.capacity})
                                </option>
                            ))}
                        </select>
                    </div>

                    <div>
                        <label className="block font-mono text-xs text-traffic-text-3 uppercase mb-2">
                            New Capacity (Original: {originalCapacity})
                        </label>
                        <input
                            type="number"
                            min="1"
                            max={originalCapacity * 2}
                            value={overrideCapacity}
                            onChange={(e) => setOverrideCapacity(parseInt(e.target.value, 10) || 0)}
                            className="w-full bg-traffic-bg border border-traffic-accent text-traffic-text px-3 py-2 font-mono text-sm"
                        />
                        <p className="text-xs text-traffic-text-3 mt-2">
                            {getCapacityTrend(originalCapacity, overrideCapacity).text}
                        </p>
                    </div>

                    <div>
                        <label className="block font-mono text-xs text-traffic-text-3 uppercase mb-2">
                            Reason
                        </label>
                        <input
                            type="text"
                            value={reason}
                            onChange={(e) => setReason(e.target.value)}
                            className="w-full bg-traffic-bg border border-traffic-accent text-traffic-text px-3 py-2 font-mono text-sm"
                            placeholder="e.g., Event management, Emergency reduction"
                        />
                    </div>

                    <div className="grid grid-cols-2 gap-4">
                        <div>
                            <label className="block font-mono text-xs text-traffic-text-3 uppercase mb-2">
                                Effective From
                            </label>
                            <input
                                type="date"
                                value={effectiveFrom}
                                onChange={(e) => setEffectiveFrom(e.target.value)}
                                className="w-full bg-traffic-bg border border-traffic-accent text-traffic-text px-3 py-2 font-mono text-sm"
                            />
                        </div>
                        <div>
                            <label className="block font-mono text-xs text-traffic-text-3 uppercase mb-2">
                                Effective Until
                            </label>
                            <input
                                type="date"
                                value={effectiveUntil}
                                onChange={(e) => setEffectiveUntil(e.target.value)}
                                className="w-full bg-traffic-bg border border-traffic-accent text-traffic-text px-3 py-2 font-mono text-sm"
                            />
                        </div>
                    </div>

                    <div className="flex gap-3">
                        <Button type="submit" variant="primary" className="flex-1">
                            Create Override
                        </Button>
                        <Button
                            type="button"
                            onClick={() => setIsFormOpen(false)}
                            className="flex-1"
                        >
                            Cancel
                        </Button>
                    </div>
                </form>
            )}

            {/* Active Overrides */}
            {error && (
                <div className="mb-4 bg-traffic-red/10 border border-traffic-red p-3 font-mono text-xs text-traffic-red">
                    {error}
                </div>
            )}
            {overrides.length > 0 ? (
                <div className="space-y-3">
                    {overrides.map((override: CapacityOverride) => {
                        const trend = getCapacityTrend(override.originalCapacity, override.overrideCapacity);
                        return (
                            <div
                                key={override.id}
                                className="bg-traffic-bg border border-traffic-border p-4 flex items-start justify-between"
                            >
                                <div className="flex-1">
                                    <div className="flex items-center gap-3 mb-2">
                                        <span className="font-barlow font-bold text-traffic-white">
                                            {override.segmentName}
                                        </span>
                                        <span className={`font-mono font-bold text-sm ${trend.color}`}>
                                            {trend.text}
                                        </span>
                                    </div>
                                    <div className="flex gap-4 mb-2 font-mono text-xs text-traffic-text-2">
                                        <span>Original: {override.originalCapacity}</span>
                                        <span>Override: {override.overrideCapacity}</span>
                                    </div>
                                    <p className="font-mono text-xs text-traffic-text-2 mb-2">
                                        Reason: {override.reason}
                                    </p>
                                    <div className="flex gap-6 font-mono text-xs text-traffic-text-3">
                                        <span>From: {override.effectiveFrom}</span>
                                        <span>Until: {override.effectiveUntil}</span>
                                        <span>By: {override.createdBy}</span>
                                    </div>
                                </div>
                                <button
                                    onClick={() => removeOverride(override.id)}
                                    className="text-traffic-red hover:text-traffic-red/70 ml-4 font-mono text-xs uppercase tracking-widest"
                                >
                                    Remove
                                </button>
                            </div>
                        );
                    })}
                </div>
            ) : (
                <p className="text-traffic-text-2 font-mono text-sm text-center py-8">
                    No active overrides
                </p>
            )}
        </div>
    );
}
