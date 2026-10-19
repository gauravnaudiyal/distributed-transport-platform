export interface PermitItem {
    id: string;
    route: string;
    origin: string;
    destination: string;
    status: 'approved' | 'denied' | 'pending';
    departureTime?: string;
    eta?: string;
    vehicleType?: string;
    capacityPercentage?: number;
    issuedAt?: string;
    reason?: string;
    alternative?: string;
}

export interface HealthCheckpoint {
    name: string;
    status: 'green' | 'amber' | 'red';
    latency: number | string;
}

export interface RegionalCapacity {
    region: string;
    capacity: number;
    status: 'green' | 'amber' | 'red';
    offline?: boolean;
}

export interface CongestedCorridor {
    route: string;
    capacity: number;
    status: 'green' | 'amber' | 'red';
}

export interface AuditTrailEvent {
    id: string;
    message: string;
    timestamp: string;
    status: 'completed' | 'pending';
}

export interface FailureSimulatorState {
    dropNotifications: boolean;
    euWestDown: boolean;
    injectLatency: boolean;
    apSPartition: boolean;
    forceStaleCache: boolean;
}

export interface UserPermitRequest {
    originSector: string;
    destinationSector: string;
    vehicleType: string;
    occupancy: string;
    window?: string;
}
