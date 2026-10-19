import type {
    PermitItem,
    HealthCheckpoint,
    RegionalCapacity,
    CongestedCorridor,
    AuditTrailEvent,
} from '../types/index';

export const mockPermits: PermitItem[] = [
    {
        id: 'PERMIT-9912',
        route: 'London → Amsterdam',
        origin: 'London, UK — Zone EU-WEST-4',
        destination: 'Amsterdam, NL — Zone EU-WEST-7',
        status: 'approved',
        departureTime: '08:20–08:35',
        eta: '11:05',
        vehicleType: 'Sedan',
        capacityPercentage: 68,
        issuedAt: '06:47:22',
    },
    {
        id: 'PERMIT-9934',
        route: 'Tokyo → Osaka',
        origin: 'Tokyo, JP',
        destination: 'Osaka, JP',
        status: 'pending',
        departureTime: '09:00',
        issuedAt: '07:12:05',
    },
    {
        id: 'PERMIT-9901',
        route: 'New York → Boston',
        origin: 'New York, USA',
        destination: 'Boston, USA',
        status: 'denied',
        reason: 'Capacity exceeded',
        alternative: 'Suggest: +2h window',
        issuedAt: '07:04:11',
    },
];

export const mockHealthCheckpoints: HealthCheckpoint[] = [
    { name: 'Booking Svc', status: 'green', latency: 42 },
    { name: 'Capacity Svc', status: 'green', latency: 61 },
    { name: 'EU-W Gateway', status: 'amber', latency: 890 },
    { name: 'Identity Svc', status: 'green', latency: 28 },
    { name: 'AP-S Gateway', status: 'red', latency: '—' },
    { name: 'Notifications', status: 'green', latency: 15 },
];

export const mockRegionalCapacity: RegionalCapacity[] = [
    { region: 'EU-WEST', capacity: 74, status: 'amber' },
    { region: 'NA-EAST', capacity: 41, status: 'green' },
    { region: 'AP-SOUTH', capacity: 100, status: 'red', offline: true },
    { region: 'SA-WEST', capacity: 28, status: 'green' },
];

export const mockCongestedCorridors: CongestedCorridor[] = [
    { route: 'NYC → BOS', capacity: 94, status: 'red' },
    { route: 'LDN → AMS', capacity: 74, status: 'amber' },
    { route: 'PAR → BER', capacity: 68, status: 'amber' },
    { route: 'TYO → OSK', capacity: 52, status: 'green' },
    { route: 'SYD → MEL', capacity: 31, status: 'green' },
];

export const mockAuditTrail: AuditTrailEvent[] = [
    {
        id: '1',
        message: 'Request received by EU-WEST gateway',
        timestamp: '07:04:01.233',
        status: 'completed',
    },
    {
        id: '2',
        message: 'Capacity service confirmed — corridor at 68%',
        timestamp: '07:04:01.611',
        status: 'completed',
    },
    {
        id: '3',
        message: 'Identity verified · Clearance CIVILIAN',
        timestamp: '07:04:01.890',
        status: 'completed',
    },
    {
        id: '4',
        message: 'Regional consensus reached (3/3 nodes)',
        timestamp: '07:04:02.104',
        status: 'completed',
    },
    {
        id: '5',
        message: 'Notification delivery pending (EU-W degraded)',
        timestamp: '07:04:02.201 · retrying…',
        status: 'pending',
    },
];

export const mockStats = {
    permitsGranted: 1204,
    permitsGrantedDelta: '+14%',
    permitsDenied: 389,
    permitsDeniedDelta: 'Capacity exceeded (61%)',
    pendingConsensus: 47,
    pendingConsensusDelta: 'Avg wait: 12s',
    globalCongestion: 74,
    globalCongestionDelta: '↑ High · Peak window',
};

export const mockTickerItems = [
    { status: 'granted', text: 'PERMIT-9912 · LDN→AMS · 08:20-08:35' },
    { status: 'denied', text: 'PERMIT-9901 · NYC→BOS · CAPACITY EXCEEDED' },
    { status: 'pending', text: 'PERMIT-9934 · TYO→OSK · AWAITING CONSENSUS' },
    { status: 'granted', text: 'PERMIT-9887 · PAR→BER · 09:00-09:15' },
    { status: 'denied', text: 'PERMIT-9845 · SYD→MEL · RESTRICTED CORRIDOR' },
    { status: 'granted', text: 'PERMIT-9910 · MNL→SGP · 07:45-08:05' },
    { status: 'warning', text: 'EU-WEST GATEWAY — DEGRADED MODE' },
];
