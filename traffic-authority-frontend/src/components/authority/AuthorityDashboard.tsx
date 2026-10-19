import { RoadClosureManager } from './RoadClosureManager';
import { CapacityOverridePanel } from './CapacityOverridePanel';
import { BookingVerificationTool } from './BookingVerificationTool';
import { useAuthorityStore } from '../../store/authority';
import { useState } from 'react';
import { authorityApiService } from '../../services/authorityApi';
import type { SegmentForecastResponse, AuthorityAuditEntry } from '../../types/authority';

export function AuthorityDashboard() {
    const { closures, overrides, restrictions } = useAuthorityStore();
    const [segmentId, setSegmentId] = useState('SEG-UK-M1-001');
    const [forecast, setForecast] = useState<SegmentForecastResponse | null>(null);
    const [segmentAudit, setSegmentAudit] = useState<AuthorityAuditEntry[]>([]);
    const [insightError, setInsightError] = useState<string | null>(null);

    const totalImpact = closures.length + overrides.length + restrictions.length;

    const handleLoadForecast = async () => {
        try {
            setInsightError(null);
            const response = await authorityApiService.getSegmentForecast(segmentId);
            setForecast(response);
        } catch (err) {
            setInsightError(
                err instanceof Error ? err.message : 'Failed to load forecast'
            );
        }
    };

    const handleLoadSegmentAudit = async () => {
        try {
            setInsightError(null);
            const response = await authorityApiService.getSegmentAudit(segmentId);
            setSegmentAudit(response);
        } catch (err) {
            setInsightError(err instanceof Error ? err.message : 'Failed to load audit');
        }
    };

    return (
        <div className="bg-traffic-bg-2 px-8 py-7">
            {/* Header */}
            <div className="mb-8">
                <h1 className="font-barlow font-black text-3xl uppercase tracking-wider text-traffic-white mb-2">
                    Traffic Authority Control Panel
                </h1>
                <p className="font-mono text-xs text-traffic-text-3 uppercase tracking-widest">
          // Regional Road Network Management & Enforcement Verification
                </p>
            </div>

            {/* Stats Overview */}
            <div className="grid grid-cols-4 gap-4 mb-8">
                <div className="bg-traffic-panel border border-traffic-border p-4">
                    <p className="font-mono text-xs text-traffic-text-3 uppercase mb-2">
                        Active Closures
                    </p>
                    <p className="font-barlow font-black text-3xl text-traffic-red">
                        {closures.length}
                    </p>
                </div>

                <div className="bg-traffic-panel border border-traffic-border p-4">
                    <p className="font-mono text-xs text-traffic-text-3 uppercase mb-2">
                        Capacity Overrides
                    </p>
                    <p className="font-barlow font-black text-3xl text-traffic-amber">
                        {overrides.length}
                    </p>
                </div>

                <div className="bg-traffic-panel border border-traffic-border p-4">
                    <p className="font-mono text-xs text-traffic-text-3 uppercase mb-2">
                        Active Restrictions
                    </p>
                    <p className="font-barlow font-black text-3xl text-traffic-accent">
                        {restrictions.length}
                    </p>
                </div>

                <div className="bg-traffic-panel border border-traffic-border p-4">
                    <p className="font-mono text-xs text-traffic-text-3 uppercase mb-2">
                        Total Active
                    </p>
                    <p className="font-barlow font-black text-3xl text-traffic-green">
                        {totalImpact}
                    </p>
                </div>
            </div>

            {/* Main Controls - Two Column Layout */}
            <div className="grid grid-cols-2 gap-6 mb-8 max-h-[calc(100vh-400px)] overflow-y-auto">
                {/* Left Column */}
                <div className="space-y-6">
                    <RoadClosureManager />
                </div>

                {/* Right Column */}
                <div className="space-y-6">
                    <CapacityOverridePanel />
                </div>
            </div>

            {/* Full-Width Verification Tool */}
            <div className="mb-8">
                <BookingVerificationTool />
            </div>

            <div className="mb-8 bg-traffic-panel border border-traffic-border p-6">
                <h2 className="font-barlow font-bold text-lg uppercase tracking-wider text-traffic-white mb-4">
                    Segment Forecast & Audit
                </h2>
                <div className="flex gap-3 mb-4">
                    <input
                        value={segmentId}
                        onChange={(e) => setSegmentId(e.target.value)}
                        className="flex-1 bg-traffic-bg border border-traffic-accent text-traffic-text px-3 py-2 font-mono text-sm"
                        placeholder="Segment ID"
                    />
                    <button
                        onClick={handleLoadForecast}
                        className="px-3 py-2 text-xs border border-traffic-accent text-traffic-accent font-mono uppercase"
                    >
                        Load Forecast
                    </button>
                    <button
                        onClick={handleLoadSegmentAudit}
                        className="px-3 py-2 text-xs border border-traffic-accent text-traffic-accent font-mono uppercase"
                    >
                        Load Audit
                    </button>
                </div>
                {insightError && (
                    <p className="font-mono text-xs text-traffic-red mb-3">{insightError}</p>
                )}
                {forecast && (
                    <div className="font-mono text-xs text-traffic-text-2 mb-4 space-y-1">
                        <p>Map version: {forecast.mapVersion}</p>
                        <p>Baseline: {forecast.baselineCapacity}</p>
                        <p>Confirmed: {forecast.confirmedBookings}</p>
                        <p>
                            Utilization: {(forecast.utilizationRatio * 100).toFixed(1)}%
                        </p>
                    </div>
                )}
                {segmentAudit.length > 0 && (
                    <div className="space-y-2">
                        {segmentAudit.slice(0, 5).map((entry) => (
                            <div
                                key={entry.id}
                                className="bg-traffic-bg border border-traffic-border p-2"
                            >
                                <p className="font-mono text-xs text-traffic-accent">
                                    {entry.instructionType} / {entry.status}
                                </p>
                                <p className="font-mono text-xs text-traffic-text-3">
                                    {new Date(entry.createdAt).toLocaleString()}
                                </p>
                            </div>
                        ))}
                    </div>
                )}
            </div>

            {/* Authority Information Footer */}
            <div className="border-t border-traffic-border pt-6 mt-8">
                <p className="font-mono text-xs text-traffic-text-2 mb-3 uppercase tracking-widest">
                    Authority Information
                </p>
                <div className="grid grid-cols-3 gap-4 font-mono text-xs text-traffic-text-3">
                    <div>
                        <p className="text-traffic-text-2 mb-2">Current Region:</p>
                        <p>UK-North, UK-South</p>
                    </div>
                    <div>
                        <p className="text-traffic-text-2 mb-2">Managed Segments:</p>
                        <p>5 highway segments</p>
                    </div>
                    <div>
                        <p className="text-traffic-text-2 mb-2">Last Update:</p>
                        <p>{new Date().toLocaleString()}</p>
                    </div>
                </div>
            </div>
        </div>
    );
}
