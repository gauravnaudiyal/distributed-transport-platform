import { useUIStore } from '../../store/ui';
import { HealthDot } from '../common/HealthDot';
import { useAuth } from '../../context/AuthContext';
import type { HealthCheckpoint } from '../../types/index';
import { cn } from '../../utils/cn';

interface SidebarProps {
    healthCheckpoints: HealthCheckpoint[];
}

export function Sidebar({ healthCheckpoints }: SidebarProps) {
    const { activeNav, setActiveNav, failures, setFailureSimulator } = useUIStore();
    const { isAdmin, isCivilian } = useAuth();

    const adminNavItems = [
        { key: 'command-center' as const, label: 'Command Center' },
        { key: 'request-passage' as const, label: 'Request Passage' },
        { key: 'my-permits' as const, label: 'My Permits' },
        { key: 'traffic-map' as const, label: 'Traffic Map' },
        { key: 'enforcement' as const, label: 'Enforcement' },
        { key: 'profile-notifications' as const, label: 'Profile & Alerts' },
    ];

    const civilianNavItems = [
        { key: 'my-permits' as const, label: 'My Permits' },
        { key: 'request-passage' as const, label: 'Request Passage' },
        { key: 'traffic-map' as const, label: 'Traffic Map' },
        { key: 'profile-notifications' as const, label: 'Profile & Alerts' },
    ];

    const navItems = isAdmin ? adminNavItems : civilianNavItems;

    return (
        <div className="bg-traffic-panel border-r border-traffic-border w-64 pt-6 flex flex-col">
            {/* Navigation Section */}
            <div className="pb-4 border-b border-traffic-border mb-2">
                <div className="font-mono text-xs text-traffic-text-3 uppercase tracking-widest px-5 pb-3">
                    Navigation
                </div>
                {navItems.map((item) => (
                    <div
                        key={item.key}
                        onClick={() => setActiveNav(item.key)}
                        className={cn(
                            'flex items-center gap-3 px-5 py-2.5 cursor-pointer transition-all duration-150 font-barlow font-semibold text-xs uppercase tracking-wider relative',
                            activeNav === item.key
                                ? 'text-traffic-green bg-traffic-green/5'
                                : 'text-traffic-text-2 hover:bg-traffic-text/4'
                        )}
                    >
                        {activeNav === item.key && (
                            <div className="absolute left-0 top-0 bottom-0 w-0.5 bg-traffic-green shadow-glow" />
                        )}
                        <div
                            className={cn(
                                'w-1.5 h-1.5 rounded-full flex-shrink-0',
                                activeNav === item.key ? 'animate-dot-pulse' : ''
                            )}
                            style={{
                                backgroundColor: activeNav === item.key ? 'rgb(0, 255, 136)' : 'currentColor',
                            }}
                        />
                        {item.label}
                        {item.key === 'request-passage' && (
                            <div className="ml-auto px-1.5 py-0.5 bg-traffic-red font-mono text-xs text-white">
                                NEW
                            </div>
                        )}
                    </div>
                ))}
            </div>

            {/* System Health Section */}
            <div className="pb-4 border-b border-traffic-border mb-2">
                <div className="font-mono text-xs text-traffic-text-3 uppercase tracking-widest px-5 pb-3">
                    System Health
                </div>
                <div className="px-5 space-y-2">
                    {healthCheckpoints.map((checkpoint) => (
                        <div key={checkpoint.name} className="flex items-center justify-between font-mono text-xs">
                            <div className="flex items-center gap-2">
                                <HealthDot status={checkpoint.status} />
                                <span className="text-traffic-text-2">{checkpoint.name}</span>
                            </div>
                            <div className="text-traffic-text-3 text-xs">
                                {typeof checkpoint.latency === 'number' ? `${checkpoint.latency}ms` : checkpoint.latency}
                            </div>
                        </div>
                    ))}
                </div>
            </div>

            {/* Failure Simulator Section - Admin Only */}
            {isAdmin && (
                <div className="pb-4">
                    <div className="font-mono text-xs text-traffic-text-3 uppercase tracking-widest px-5 pb-3">
                        Failure Simulator
                    </div>
                    <div className="px-5 space-y-2">
                        {[
                            { key: 'dropNotifications', label: 'Drop Notifications' },
                            { key: 'euWestDown', label: 'EU-WEST Down' },
                            { key: 'injectLatency', label: 'Inject 2s Latency' },
                            { key: 'apSPartition', label: 'AP-S Partition' },
                            { key: 'forceStaleCache', label: 'Force Stale Cache' },
                        ].map((item) => (
                            <div key={item.key} className="flex items-center justify-between">
                                <div className="font-mono text-xs text-traffic-text-2">{item.label}</div>
                                <button
                                    onClick={() =>
                                        setFailureSimulator(
                                            item.key as keyof typeof failures,
                                            !failures[item.key as keyof typeof failures]
                                        )
                                    }
                                    className={cn(
                                        'w-8 h-4 rounded-full relative cursor-pointer transition-all duration-200',
                                        failures[item.key as keyof typeof failures]
                                            ? 'bg-traffic-red'
                                            : 'bg-traffic-border-2'
                                    )}
                                >
                                    <div
                                        className={cn(
                                            'w-3 h-3 rounded-full bg-white absolute top-0.5 transition-transform duration-200',
                                            failures[item.key as keyof typeof failures]
                                                ? 'translate-x-4 left-0'
                                                : 'left-0.5'
                                        )}
                                    />
                                </button>
                            </div>
                        ))}
                    </div>
                </div>
            )}
        </div>
    );
}
