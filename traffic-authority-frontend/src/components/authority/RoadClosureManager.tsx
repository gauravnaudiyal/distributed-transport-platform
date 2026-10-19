import { useState } from 'react';
import type { RoadClosure } from '../../store/authority';
import { useAuthorityStore } from '../../store/authority';
import { authorityApiService } from '../../services/authorityApi';
import { Button } from '../common/Button';

export function RoadClosureManager() {
    const {
        closures,
        addClosure,
        removeClosure,
        setPolicyUpdateInFlight,
        setLastPolicyApplyAt,
        setError,
        error,
    } = useAuthorityStore();
    const [isFormOpen, setIsFormOpen] = useState(false);
    const [segmentId, setSegmentId] = useState('SEG-UK-M1-001');
    const [reason, setReason] = useState('Maintenance');
    const [severity, setSeverity] = useState<'critical' | 'high' | 'medium' | 'low'>('high');
    const [startTime, setStartTime] = useState(
        new Date().toISOString().split('T')[0]
    );
    const [endTime, setEndTime] = useState(
        new Date(Date.now() + 24 * 3600000).toISOString().split('T')[0]
    );

    const segmentOptions = [
        { id: 'SEG-UK-M1-001', name: 'M1 Motorway - North Section' },
        { id: 'SEG-UK-M25-001', name: 'M25 Orbital - East Section' },
        { id: 'SEG-FR-A6-001', name: 'Autoroute A6 - Île-de-France' },
        { id: 'SEG-DE-A9-001', name: 'Autobahn A9 - Bavaria' },
        { id: 'SEG-US-I95-001', name: 'Interstate 95 - Northeast Corridor' },
    ];

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        const segment = segmentOptions.find((s) => s.id === segmentId);
        if (!segment) return;

        const closure: RoadClosure = {
            id: `CLOSURE-${Date.now()}`,
            segmentId,
            segmentName: segment.name,
            reason,
            startTime,
            endTime,
            severity,
            createdBy: 'Authority Admin',
            createdAt: new Date().toISOString(),
        };

        try {
            setPolicyUpdateInFlight(true);
            setError(null);
            await authorityApiService.createClosure({
                correlationId: `closure-${Date.now()}`,
                jurisdictionId: 'UK-NORTH',
                segmentIds: [segmentId],
                effectiveFrom: new Date(startTime).toISOString(),
                effectiveUntil: new Date(endTime).toISOString(),
                reason,
                incidentReference: `INC-${Date.now()}`,
            });
            addClosure(closure);
            setLastPolicyApplyAt(new Date().toISOString());
            setIsFormOpen(false);
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Failed to apply closure');
        } finally {
            setPolicyUpdateInFlight(false);
        }
    };

    const severityColors = {
        critical: 'bg-traffic-red text-traffic-red',
        high: 'bg-traffic-red/70 text-traffic-red',
        medium: 'bg-traffic-amber text-traffic-amber',
        low: 'bg-traffic-accent text-traffic-accent',
    };

    return (
        <div className="bg-traffic-panel border border-traffic-border p-6">
            <div className="flex items-center justify-between mb-6">
                <div>
                    <h2 className="font-barlow font-bold text-lg uppercase tracking-wider text-traffic-white mb-1">
                        Road Closures & Restrictions
                    </h2>
                    <p className="font-mono text-xs text-traffic-text-2">
            // Manage temporary road closures
                    </p>
                </div>
                {!isFormOpen && (
                    <Button onClick={() => setIsFormOpen(true)} variant="primary">
                        + New Closure
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
                                    {seg.name}
                                </option>
                            ))}
                        </select>
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
                            placeholder="e.g., Maintenance, Accident, Event"
                        />
                    </div>

                    <div className="grid grid-cols-2 gap-4">
                        <div>
                            <label className="block font-mono text-xs text-traffic-text-3 uppercase mb-2">
                                Start Date
                            </label>
                            <input
                                type="date"
                                value={startTime}
                                onChange={(e) => setStartTime(e.target.value)}
                                className="w-full bg-traffic-bg border border-traffic-accent text-traffic-text px-3 py-2 font-mono text-sm"
                            />
                        </div>
                        <div>
                            <label className="block font-mono text-xs text-traffic-text-3 uppercase mb-2">
                                End Date
                            </label>
                            <input
                                type="date"
                                value={endTime}
                                onChange={(e) => setEndTime(e.target.value)}
                                className="w-full bg-traffic-bg border border-traffic-accent text-traffic-text px-3 py-2 font-mono text-sm"
                            />
                        </div>
                    </div>

                    <div>
                        <label className="block font-mono text-xs text-traffic-text-3 uppercase mb-2">
                            Severity
                        </label>
                        <select
                            value={severity}
                            onChange={(e) => setSeverity(e.target.value as RoadClosure['severity'])}
                            className="w-full bg-traffic-bg border border-traffic-accent text-traffic-text px-3 py-2 font-mono text-sm"
                        >
                            <option value="critical">Critical</option>
                            <option value="high">High</option>
                            <option value="medium">Medium</option>
                            <option value="low">Low</option>
                        </select>
                    </div>

                    <div className="flex gap-3">
                        <Button type="submit" variant="primary" className="flex-1">
                            Create Closure
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

            {/* Active Closures */}
            {error && (
                <div className="mb-4 bg-traffic-red/10 border border-traffic-red p-3 font-mono text-xs text-traffic-red">
                    {error}
                </div>
            )}
            {closures.length > 0 ? (
                <div className="space-y-3">
                    {closures.map((closure: RoadClosure) => (
                        <div
                            key={closure.id}
                            className="bg-traffic-bg border border-traffic-border p-4 flex items-start justify-between"
                        >
                            <div className="flex-1">
                                <div className="flex items-center gap-3 mb-2">
                                    <div
                                        className={`px-2 py-1 text-xs font-bold uppercase tracking-widest border ${severityColors[closure.severity]
                                            }`}
                                    >
                                        {closure.severity}
                                    </div>
                                    <span className="font-barlow font-bold text-traffic-white">
                                        {closure.segmentName}
                                    </span>
                                </div>
                                <p className="font-mono text-xs text-traffic-text-2 mb-2">
                                    Reason: {closure.reason}
                                </p>
                                <div className="flex gap-6 font-mono text-xs text-traffic-text-3">
                                    <span>Start: {closure.startTime}</span>
                                    <span>End: {closure.endTime}</span>
                                    <span>By: {closure.createdBy}</span>
                                </div>
                            </div>
                            <button
                                onClick={() => removeClosure(closure.id)}
                                className="text-traffic-red hover:text-traffic-red/70 ml-4 font-mono text-xs uppercase tracking-widest"
                            >
                                Remove
                            </button>
                        </div>
                    ))}
                </div>
            ) : (
                <p className="text-traffic-text-2 font-mono text-sm text-center py-8">
                    No active closures
                </p>
            )}
        </div>
    );
}
