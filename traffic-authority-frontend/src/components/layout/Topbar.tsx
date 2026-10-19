import { useAuth } from '../../context/AuthContext';
import { useEffect, useState } from 'react';

export function Topbar() {
    const auth = useAuth();
    const [gatewayHealth, setGatewayHealth] = useState<'up' | 'down' | 'checking'>(
        'checking'
    );
    const [lastChecked, setLastChecked] = useState<string>('');

    useEffect(() => {
        const healthUrl =
            `${import.meta.env.VITE_API_BASE_URL || import.meta.env.VITE_API_BASE || '/api'}/health`;
        let isMounted = true;

        const checkHealth = async () => {
            try {
                const response = await fetch(healthUrl, { cache: 'no-store' });
                if (!isMounted) return;
                setGatewayHealth(response.ok || response.status === 401 ? 'up' : 'down');
                setLastChecked(new Date().toLocaleTimeString());
            } catch {
                if (!isMounted) return;
                setGatewayHealth('down');
                setLastChecked(new Date().toLocaleTimeString());
            }
        };

        void checkHealth();
        const interval = setInterval(checkHealth, 20000);

        return () => {
            isMounted = false;
            clearInterval(interval);
        };
    }, []);

    return (
        <div className="flex items-center justify-between px-8 h-[52px] bg-traffic-panel border-b border-traffic-border relative overflow-hidden">
            {/* Animated top border */}
            <div
                className="absolute bottom-0 left-0 right-0 h-0.5 bg-gradient-to-r from-transparent via-traffic-red to-transparent"
                style={{
                    animation: 'scanH 4s ease-in-out infinite',
                }}
            />

            {/* Logo Section */}
            <div className="flex items-center gap-3.5">
                <div className="w-8 h-8 border-2 border-traffic-red flex items-center justify-center font-mono text-sm text-traffic-red relative animate-crest-pulse">
                    TA
                </div>
                <div>
                    <div className="font-barlow font-black text-base uppercase tracking-widest text-traffic-white">
                        The Traffic Authority
                    </div>
                    <div className="font-mono text-xs text-traffic-text-3 uppercase tracking-widest">
                        Movement Control Division — Sector 7
                    </div>
                </div>
            </div>

            {/* Center Status */}
            <div className="font-mono text-xs uppercase tracking-wide">
                <span className="text-traffic-amber animate-blink mr-4">
                    ● PEAK-HOUR RESTRICTIONS ACTIVE — EU-WEST / NA-EAST ●
                </span>
                <span
                    className={
                        gatewayHealth === 'up'
                            ? 'text-traffic-green'
                            : gatewayHealth === 'down'
                                ? 'text-traffic-red'
                                : 'text-traffic-text-2'
                    }
                >
                    GW:{' '}
                    {gatewayHealth === 'checking'
                        ? 'CHECKING'
                        : gatewayHealth.toUpperCase()}
                </span>
                {lastChecked && (
                    <span className="text-traffic-text-3 ml-2">[{lastChecked}]</span>
                )}
            </div>

            {/* Right Section */}
            <div className="flex items-center gap-4">
                {auth.isLoggedIn ? (
                    <>
                        <div className="font-mono text-xs text-traffic-text-2 uppercase tracking-wide">
                            {auth.username}
                        </div>
                        <div className={`px-2.5 py-0.5 border font-mono text-xs uppercase tracking-widest ${auth.isAdmin ? 'border-traffic-red text-traffic-red' : 'border-traffic-blue text-traffic-blue'
                            }`}>
                            {auth.userRole}
                        </div>
                        <button
                            onClick={auth.toggleTestRole}
                            className="text-xs bg-traffic-accent/20 border border-traffic-accent px-2 py-1 rounded hover:bg-traffic-accent/40 transition-colors text-traffic-accent"
                            title="Toggle role for testing"
                        >
                            Toggle
                        </button>
                        <button
                            onClick={auth.logout}
                            className="text-xs bg-traffic-red/20 border border-traffic-red px-2 py-1 rounded hover:bg-traffic-red/40 transition-colors text-traffic-red"
                        >
                            Logout
                        </button>
                    </>
                ) : (
                    <div className="font-mono text-xs text-traffic-text-2 uppercase tracking-wide">
                        Not logged in
                    </div>
                )}
            </div>
        </div>
    );
}
