export interface AppliedInstructionResponse {
    mapVersion: number;
    correlationId: string;
    policyReference: string;
}

export interface AuthorityClosureRequest {
    correlationId: string;
    jurisdictionId: string;
    segmentIds: string[];
    effectiveFrom: string;
    effectiveUntil?: string;
    reason?: string;
    incidentReference?: string;
}

export interface AuthorityRestrictionRequest {
    correlationId: string;
    jurisdictionId: string;
    segmentIds: string[];
    effectiveFrom: string;
    effectiveUntil?: string;
    restrictionType: string;
    parametersJson?: string;
    reason?: string;
}

export interface AuthorityCapacityOverrideRequest {
    correlationId: string;
    jurisdictionId: string;
    segmentIds: string[];
    effectiveFrom: string;
    effectiveUntil?: string;
    capacityLimit: number;
    reason?: string;
}

export interface AuthorityBookingVerificationResponse {
    bookingId: string;
    status: string;
    mapVersion: number;
    windowStart: string;
    windowEnd: string;
    segmentIds: string[];
    proofReference: string;
}

export interface SegmentForecastResponse {
    segmentId: string;
    mapVersion: number;
    baselineCapacity: number;
    provisionalHolds: number;
    confirmedBookings: number;
    utilizationRatio: number;
}

export interface AuthorityAuditEntry {
    id: string;
    instructionType: string;
    correlationId: string;
    segmentIds: string[];
    mapVersion: number;
    status: string;
    createdAt: string;
    externalRef: string;
}
