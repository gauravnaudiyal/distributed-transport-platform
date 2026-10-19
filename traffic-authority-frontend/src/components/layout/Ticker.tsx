import { useEffect, useRef } from 'react';
import { mockTickerItems } from '../../utils/mockData';

export function Ticker() {
    const tickerRef = useRef<HTMLDivElement>(null);

    const getStatusColor = (status: string) => {
        switch (status) {
            case 'granted':
                return 'text-traffic-green';
            case 'denied':
                return 'text-traffic-red-2';
            case 'pending':
                return 'text-traffic-amber-2';
            case 'warning':
                return 'text-traffic-amber-2';
            default:
                return 'text-traffic-text-3';
        }
    };

    const getStatusSymbol = (status: string) => {
        switch (status) {
            case 'granted':
                return '✓ GRANTED';
            case 'denied':
                return '✗ DENIED';
            case 'pending':
                return '⏳ PENDING';
            case 'warning':
                return '⚠ WARNING';
            default:
                return '';
        }
    };

    return (
        <div className="border-t border-b border-traffic-border py-2 px-0 bg-traffic-bg/20 overflow-hidden">
            <div
                ref={tickerRef}
                className="flex gap-10 animate-tickerScroll whitespace-nowrap"
            >
                {mockTickerItems.map((item, i) => (
                    <div key={i} className="font-mono text-xs text-traffic-text-3 uppercase tracking-wide flex items-center gap-2 flex-shrink-0">
                        <span className={getStatusColor(item.status)}>
                            {getStatusSymbol(item.status)}
                        </span>
                        <span>{item.text}</span>
                    </div>
                ))}
                {/* Duplicate for seamless loop */}
                {mockTickerItems.map((item, i) => (
                    <div key={`dup-${i}`} className="font-mono text-xs text-traffic-text-3 uppercase tracking-wide flex items-center gap-2 flex-shrink-0">
                        <span className={getStatusColor(item.status)}>
                            {getStatusSymbol(item.status)}
                        </span>
                        <span>{item.text}</span>
                    </div>
                ))}
            </div>
        </div>
    );
}
